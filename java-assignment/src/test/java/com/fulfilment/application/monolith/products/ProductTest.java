package com.fulfilment.application.monolith.products;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void defaultConstructor() {
        Product p = new Product();
        assertNull(p.id);
        assertNull(p.name);
        assertNull(p.description);
        assertNull(p.price);
        assertEquals(0, p.stock);
    }

    @Test
    void nameConstructor() {
        Product p = new Product("Widget");
        assertEquals("Widget", p.name);
        assertNull(p.id);
        assertNull(p.description);
        assertNull(p.price);
        assertEquals(0, p.stock);
    }

    @Test
    void fields() {
        Product p = new Product();
        p.name = "Gadget";
        p.description = "A fancy gadget";
        p.price = new BigDecimal("99.99");
        p.stock = 10;
        assertEquals("Gadget", p.name);
        assertEquals("A fancy gadget", p.description);
        assertEquals(new BigDecimal("99.99"), p.price);
        assertEquals(10, p.stock);
    }
}
