package com.fulfilment.application.monolith.stores;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StoreTransactionEventTest {

    @Test
    void constructorAndGetters() {
        Store store = new Store("EventStore");
        store.quantityProductsInStock = 3;
        StoreTransactionEvent event = new StoreTransactionEvent(store, StoreTransactionEvent.Type.UPDATED);

        assertEquals(store, event.store);
        assertEquals(StoreTransactionEvent.Type.UPDATED, event.type);
    }

    @Test
    void typeEnumValues() {
        assertEquals(2, StoreTransactionEvent.Type.values().length);
        assertNotNull(StoreTransactionEvent.Type.valueOf("CREATED"));
        assertNotNull(StoreTransactionEvent.Type.valueOf("UPDATED"));
    }
}
