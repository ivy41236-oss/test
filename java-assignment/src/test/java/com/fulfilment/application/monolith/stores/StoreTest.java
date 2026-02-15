package com.fulfilment.application.monolith.stores;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StoreTest {

    @Test
    void defaultConstructor() {
        Store s = new Store();
        assertNull(s.id);
        assertNull(s.name);
        assertEquals(0, s.quantityProductsInStock);
    }

    @Test
    void nameConstructor() {
        Store s = new Store("MainStore");
        assertEquals("MainStore", s.name);
        assertNull(s.id);
        assertEquals(0, s.quantityProductsInStock);
    }

    @Test
    void fields() {
        Store s = new Store();
        s.name = "Branch";
        s.quantityProductsInStock = 42;
        assertEquals("Branch", s.name);
        assertEquals(42, s.quantityProductsInStock);
    }
}
