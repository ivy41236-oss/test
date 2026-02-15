package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class StoreTransactionObserverTest {

    @Inject
    StoreTransactionObserver observer;

    @Test
    void smoke() {
        assertNotNull(observer);
    }

    @Test
    void afterStoreTransactionEvent() {
        Store store = new Store("Observed");
        store.quantityProductsInStock = 5;
        StoreTransactionEvent event = new StoreTransactionEvent(store, StoreTransactionEvent.Type.CREATED);

        assertDoesNotThrow(() -> observer.afterStoreTransaction(event));
    }
}
