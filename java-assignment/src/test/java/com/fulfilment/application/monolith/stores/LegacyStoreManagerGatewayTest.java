package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class LegacyStoreManagerGatewayTest {

    @Inject
    LegacyStoreManagerGateway gateway;

    @Test
    void smoke() {
        assertNotNull(gateway);
    }

    @Test
    void createStoreOnLegacySystem() {
        Store s = new Store("LegacyTest");
        s.quantityProductsInStock = 3;
        assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(s));
    }

    @Test
    void updateStoreOnLegacySystem() {
        Store s = new Store("LegacyUpdate");
        s.quantityProductsInStock = 7;
        assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(s));
    }
}
