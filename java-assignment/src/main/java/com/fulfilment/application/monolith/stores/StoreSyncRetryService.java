package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import org.jboss.logging.Logger;

@ApplicationScoped
public class StoreSyncRetryService {

  private static final Logger LOGGER = Logger.getLogger(StoreSyncRetryService.class.getName());
  private static final int MAX_ATTEMPTS = 8;
  private static final long INITIAL_DELAY_SECONDS = 5L;
  private static final long MAX_BACKOFF_SECONDS = 300L;

  @Inject LegacyStoreManagerGateway legacyStoreManagerGateway;

  @Transactional
  public void enqueue(StoreTransactionEvent event, Throwable failure) {
    if (event == null || event.store == null || event.type == null) {
      LOGGER.warn("Unable to enqueue retry job because event payload is invalid");
      return;
    }

    Instant now = Instant.now();
    StoreSyncRetryJob job = new StoreSyncRetryJob();
    job.storeId = event.store.id;
    job.storeName = event.store.name;
    job.quantityProductsInStock = event.store.quantityProductsInStock;
    job.eventType = event.type;
    job.status = StoreSyncRetryJob.Status.PENDING;
    job.attemptCount = 0;
    job.lastError = safeErrorMessage(failure);
    job.nextAttemptAt = now.plusSeconds(INITIAL_DELAY_SECONDS);
    job.createdAt = now;
    job.updatedAt = now;
    job.persist();

    // Integration hook:
    // Emit metric/log event here for dashboards and error-tracking apps
    // (for example Prometheus counter + Sentry/Bugsnag event tagged with job.id/storeId).
    LOGGER.errorf(
        "Queued retry job for legacy store sync. jobId=%d storeId=%s eventType=%s reason=%s",
        job.id, job.storeId, job.eventType, job.lastError);
  }

  @Transactional
  public void processDueJobs(int batchSize) {
    List<StoreSyncRetryJob> dueJobs = StoreSyncRetryJob.findDue(batchSize, Instant.now());

    for (StoreSyncRetryJob dueJob : dueJobs) {
      processOne(dueJob.id);
    }
  }

  @Transactional
  void processOne(Long jobId) {
    if (!claim(jobId)) {
      return;
    }

    StoreSyncRetryJob job = StoreSyncRetryJob.findById(jobId);
    if (job == null) {
      return;
    }

    try {
      dispatch(job);
      markSucceeded(job);
    } catch (Exception exception) {
      markFailed(job, exception);
    }
  }

  private boolean claim(Long jobId) {
    int updated =
        StoreSyncRetryJob.update(
            "status = ?1, updatedAt = ?2 where id = ?3 and status in (?4, ?5)",
            StoreSyncRetryJob.Status.PROCESSING,
            Instant.now(),
            jobId,
            StoreSyncRetryJob.Status.PENDING,
            StoreSyncRetryJob.Status.FAILED);
    return updated == 1;
  }

  private void dispatch(StoreSyncRetryJob job) {
    Store store = new Store();
    store.id = job.storeId;
    store.name = job.storeName;
    store.quantityProductsInStock = job.quantityProductsInStock;

    switch (job.eventType) {
      case CREATED:
        legacyStoreManagerGateway.createStoreOnLegacySystem(store);
        break;
      case UPDATED:
        legacyStoreManagerGateway.updateStoreOnLegacySystem(store);
        break;
    }
  }

  private void markSucceeded(StoreSyncRetryJob job) {
    job.attemptCount = job.attemptCount + 1;
    job.status = StoreSyncRetryJob.Status.SUCCEEDED;
    job.lastError = null;
    job.updatedAt = Instant.now();
    job.nextAttemptAt = null;
    job.persistAndFlush();
  }

  private void markFailed(StoreSyncRetryJob job, Exception exception) {
    int nextAttempt = job.attemptCount + 1;
    job.attemptCount = nextAttempt;
    job.lastError = safeErrorMessage(exception);
    job.updatedAt = Instant.now();

    if (nextAttempt >= MAX_ATTEMPTS) {
      job.status = StoreSyncRetryJob.Status.DEAD_LETTER;
      job.nextAttemptAt = null;

      // Integration hook:
      // This is the place to send a high-priority alert to monitoring/incident tools
      // because automatic retries are exhausted.
      LOGGER.errorf(
          "Retry exhausted for legacy store sync. jobId=%d storeId=%s eventType=%s attempts=%d error=%s",
          job.id, job.storeId, job.eventType, nextAttempt, job.lastError);
    } else {
      job.status = StoreSyncRetryJob.Status.FAILED;
      job.nextAttemptAt = Instant.now().plusSeconds(calculateBackoffSeconds(nextAttempt));
      LOGGER.warnf(
          "Legacy store sync retry failed. jobId=%d storeId=%s eventType=%s attempts=%d nextAttemptAt=%s error=%s",
          job.id, job.storeId, job.eventType, nextAttempt, job.nextAttemptAt, job.lastError);
    }
    job.persistAndFlush();
  }

  private long calculateBackoffSeconds(int attempt) {
    long exponential = INITIAL_DELAY_SECONDS * (1L << Math.min(attempt - 1, 10));
    return Math.min(exponential, MAX_BACKOFF_SECONDS);
  }

  private String safeErrorMessage(Throwable throwable) {
    if (throwable == null || throwable.getMessage() == null) {
      return "n/a";
    }
    String raw = throwable.getMessage().trim();
    if (raw.isEmpty()) {
      return "n/a";
    }
    if (raw.length() <= 1024) {
      return raw;
    }
    return raw.substring(0, 1024);
  }
}
