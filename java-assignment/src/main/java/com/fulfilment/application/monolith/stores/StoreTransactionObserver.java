package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class StoreTransactionObserver {

  private static final Logger LOGGER = Logger.getLogger(StoreTransactionObserver.class.getName());

  @Inject LegacyStoreManagerGateway legacyStoreManagerGateway;
  @Inject StoreSyncRetryService storeSyncRetryService;

  public void afterStoreTransaction(
      @Observes(during = TransactionPhase.AFTER_SUCCESS) StoreTransactionEvent event) {
    if (event == null || event.store == null || event.type == null) {
      LOGGER.warn("Invalid StoreTransactionEvent received - skipping legacy system call");
      return;
    }

    try {
      switch (event.type) {
        case CREATED:
          legacyStoreManagerGateway.createStoreOnLegacySystem(event.store);
          break;
        case UPDATED:
          legacyStoreManagerGateway.updateStoreOnLegacySystem(event.store);
          break;
      }
    } catch (Exception exception) {
      LOGGER.error(
          String.format(
              "Immediate legacy sync failed after transaction commit. storeId=%s eventType=%s",
              event.store.id, event.type),
          exception);
      storeSyncRetryService.enqueue(event, exception);
    }
  }
}
