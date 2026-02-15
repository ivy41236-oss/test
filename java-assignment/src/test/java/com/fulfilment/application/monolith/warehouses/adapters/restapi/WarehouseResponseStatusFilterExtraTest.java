package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class WarehouseResponseStatusFilterExtraTest {

    @Inject
    WarehouseResponseStatusFilter filter;

    @Test
    void smoke() {
        assertNotNull(filter);
    }

    @Test
    void filter() {
        jakarta.ws.rs.container.ContainerResponseContext ctx = mock(jakarta.ws.rs.container.ContainerResponseContext.class);
        jakarta.ws.rs.container.ContainerRequestContext req = mock(jakarta.ws.rs.container.ContainerRequestContext.class);

        when(ctx.getStatus()).thenReturn(200);
        when(req.getUri()).thenReturn(java.net.URI.create("/warehouse"));
        when(ctx.getEntity()).thenReturn(new com.warehouse.api.beans.Warehouse());

        assertDoesNotThrow(() -> filter.filter(req, ctx));
    }
}
