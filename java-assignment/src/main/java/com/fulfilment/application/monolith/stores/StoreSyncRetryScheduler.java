package com.fulfilment.application.monolith.stores;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class StoreSyncRetryScheduler {

  @Inject StoreSyncRetryService storeSyncRetryService;

  @Scheduled(every = "${stores.sync.retry.interval:10s}")
  void processRetries() {
    storeSyncRetryService.processDueJobs(20);
  }
}
