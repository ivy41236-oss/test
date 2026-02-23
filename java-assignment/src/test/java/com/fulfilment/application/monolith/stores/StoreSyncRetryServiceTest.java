package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

@QuarkusTest
class StoreSyncRetryServiceTest {

  @Inject StoreSyncRetryService storeSyncRetryService;

  @Mock LegacyStoreManagerGateway legacyStoreManagerGateway;

  @Test
  @TestTransaction
  void processDueJobs_shouldMarkSucceeded() {
    StoreSyncRetryJob job = buildDueJob(StoreTransactionEvent.Type.CREATED);
    job.persistAndFlush();

    storeSyncRetryService.processDueJobs(10);

    StoreSyncRetryJob reloaded = StoreSyncRetryJob.findById(job.id);
    assertNotNull(reloaded);
    assertEquals(StoreSyncRetryJob.Status.SUCCEEDED, reloaded.status);
    assertEquals(1, reloaded.attemptCount);
    assertNull(reloaded.lastError);
    Mockito.verify(legacyStoreManagerGateway, Mockito.times(1))
        .createStoreOnLegacySystem(Mockito.argThat(s -> s != null && "Retry-Store".equals(s.name)));
  }

  @Test
  @TestTransaction
  void processDueJobs_shouldRescheduleOnFailure() {
    StoreSyncRetryJob job = buildDueJob(StoreTransactionEvent.Type.UPDATED);
    job.persistAndFlush();
    Mockito.doThrow(new RuntimeException("legacy down"))
        .when(legacyStoreManagerGateway)
        .updateStoreOnLegacySystem(Mockito.any(Store.class));

    storeSyncRetryService.processDueJobs(10);

    StoreSyncRetryJob reloaded = StoreSyncRetryJob.findById(job.id);
    assertNotNull(reloaded);
    assertEquals(StoreSyncRetryJob.Status.FAILED, reloaded.status);
    assertEquals(1, reloaded.attemptCount);
    assertNotNull(reloaded.lastError);
    assertNotNull(reloaded.nextAttemptAt);
    assertTrue(reloaded.nextAttemptAt.isAfter(Instant.now().minusSeconds(1)));
    Mockito.verify(legacyStoreManagerGateway, Mockito.times(1))
        .updateStoreOnLegacySystem(Mockito.any(Store.class));
  }

  private StoreSyncRetryJob buildDueJob(StoreTransactionEvent.Type type) {
    StoreSyncRetryJob job = new StoreSyncRetryJob();
    job.storeId = 999L;
    job.storeName = "Retry-Store";
    job.quantityProductsInStock = 11;
    job.eventType = type;
    job.status = StoreSyncRetryJob.Status.PENDING;
    job.attemptCount = 0;
    job.lastError = null;
    job.createdAt = Instant.now().minusSeconds(30);
    job.updatedAt = job.createdAt;
    job.nextAttemptAt = Instant.now().minusSeconds(5);
    return job;
  }
}
