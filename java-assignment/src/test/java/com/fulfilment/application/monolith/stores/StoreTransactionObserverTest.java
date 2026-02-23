package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class StoreTransactionObserverTest {

    @Inject
    StoreTransactionObserver observer;
    @Mock
    LegacyStoreManagerGateway legacyStoreManagerGateway;
    @Mock
    StoreSyncRetryService storeSyncRetryService;

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
        Mockito.verify(legacyStoreManagerGateway, Mockito.times(1))
                .createStoreOnLegacySystem(store);
        Mockito.verifyNoInteractions(storeSyncRetryService);
    }

    @Test
    void afterStoreTransactionEventFailure_shouldEnqueueRetry() {
        Store store = new Store("Observed-Failure");
        StoreTransactionEvent event = new StoreTransactionEvent(store, StoreTransactionEvent.Type.CREATED);
        RuntimeException failure = new RuntimeException("legacy down");

        Mockito.doThrow(failure).when(legacyStoreManagerGateway).createStoreOnLegacySystem(store);

        assertDoesNotThrow(() -> observer.afterStoreTransaction(event));

        Mockito.verify(storeSyncRetryService, Mockito.times(1))
                .enqueue(Mockito.eq(event), Mockito.eq(failure));
    }
}
