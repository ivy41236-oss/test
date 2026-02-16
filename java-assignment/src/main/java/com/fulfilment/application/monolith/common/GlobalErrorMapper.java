package com.fulfilment.application.monolith.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class GlobalErrorMapper implements ExceptionMapper<Exception> {

  private static final Logger LOGGER = Logger.getLogger(GlobalErrorMapper.class.getName());

  @Inject ObjectMapper objectMapper;

  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public Response toResponse(Exception exception) {
    LOGGER.error("Failed to handle request", exception);

    int code = 500;
    if (exception instanceof WebApplicationException webEx
        && webEx.getResponse() != null
        && webEx.getResponse().getStatus() > 0) {
      code = webEx.getResponse().getStatus();
    }

    ObjectNode exceptionJson = objectMapper.createObjectNode();
    exceptionJson.put("exceptionType", exception.getClass().getName());
    exceptionJson.put("code", code);

    if (exception.getMessage() != null) {
      exceptionJson.put("error", exception.getMessage());
    }

    return Response.status(code).entity(exceptionJson).build();
  }
}
