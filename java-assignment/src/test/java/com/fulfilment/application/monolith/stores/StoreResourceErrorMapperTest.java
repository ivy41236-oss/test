package com.fulfilment.application.monolith.stores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StoreResourceErrorMapperTest {

    @Test
    void toResponseWithWebApplicationException() {
        StoreResource.ErrorMapper mapper = new StoreResource.ErrorMapper();
        mapper.objectMapper = new ObjectMapper();

        WebApplicationException ex = new WebApplicationException("Store not found", 404);
        Response response = mapper.toResponse(ex);

        assertEquals(404, response.getStatus());
        ObjectNode json = (ObjectNode) response.getEntity();
        assertEquals("jakarta.ws.rs.WebApplicationException", json.get("exceptionType").asText());
        assertEquals(404, json.get("code").asInt());
        assertEquals("Store not found", json.get("error").asText());
    }

    @Test
    void toResponseWithGenericException() {
        StoreResource.ErrorMapper mapper = new StoreResource.ErrorMapper();
        mapper.objectMapper = new ObjectMapper();

        RuntimeException ex = new RuntimeException("Store error");
        Response response = mapper.toResponse(ex);

        assertEquals(500, response.getStatus());
        ObjectNode json = (ObjectNode) response.getEntity();
        assertEquals("java.lang.RuntimeException", json.get("exceptionType").asText());
        assertEquals(500, json.get("code").asInt());
        assertEquals("Store error", json.get("error").asText());
    }

    @Test
    void toResponseWithNullExceptionMessage() {
        StoreResource.ErrorMapper mapper = new StoreResource.ErrorMapper();
        mapper.objectMapper = new ObjectMapper();

        WebApplicationException ex = new WebApplicationException((String) null);
        Response response = mapper.toResponse(ex);

        assertEquals(500, response.getStatus());
        ObjectNode json = (ObjectNode) response.getEntity();
        assertFalse(json.has("error"));
    }
}
