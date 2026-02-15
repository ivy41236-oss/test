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

  public void afterStoreTransaction(
      @Observes(during = TransactionPhase.AFTER_SUCCESS) StoreTransactionEvent event) {
    if (event == null || event.store == null || event.type == null) {
      LOGGER.warn("Invalid StoreTransactionEvent received - skipping legacy system call");
      return;
    }

    switch (event.type) {
      case CREATED:
        legacyStoreManagerGateway.createStoreOnLegacySystem(event.store);
        break;
      case UPDATED:
        legacyStoreManagerGateway.updateStoreOnLegacySystem(event.store);
        break;
    }
  }
}
