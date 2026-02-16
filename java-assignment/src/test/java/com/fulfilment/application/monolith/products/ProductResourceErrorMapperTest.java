package com.fulfilment.application.monolith.products;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fulfilment.application.monolith.common.GlobalErrorMapper;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductResourceErrorMapperTest {

    @Test
    void toResponseWithWebApplicationException() {
        GlobalErrorMapper mapper = new GlobalErrorMapper();
        mapper.setObjectMapper(new ObjectMapper());

        WebApplicationException ex = new WebApplicationException("Not found", 404);
        Response response = mapper.toResponse(ex);

        assertEquals(404, response.getStatus());
        ObjectNode json = (ObjectNode) response.getEntity();
        assertEquals("jakarta.ws.rs.WebApplicationException", json.get("exceptionType").asText());
        assertEquals(404, json.get("code").asInt());
        assertEquals("Not found", json.get("error").asText());
    }

    @Test
    void toResponseWithGenericException() {
        GlobalErrorMapper mapper = new GlobalErrorMapper();
        mapper.setObjectMapper(new ObjectMapper());

        RuntimeException ex = new RuntimeException("Boom");
        Response response = mapper.toResponse(ex);

        assertEquals(500, response.getStatus());
        ObjectNode json = (ObjectNode) response.getEntity();
        assertEquals("java.lang.RuntimeException", json.get("exceptionType").asText());
        assertEquals(500, json.get("code").asInt());
        assertEquals("Boom", json.get("error").asText());
    }

    @Test
    void toResponseWithNullExceptionMessage() {
        GlobalErrorMapper mapper = new GlobalErrorMapper();
        mapper.setObjectMapper(new ObjectMapper());

        WebApplicationException ex = new WebApplicationException((String) null);
        Response response = mapper.toResponse(ex);

        assertEquals(500, response.getStatus());
        ObjectNode json = (ObjectNode) response.getEntity();
        assertFalse(json.has("error"));
    }
}
