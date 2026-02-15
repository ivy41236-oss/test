package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class WarehouseResponseStatusFilter implements ContainerResponseFilter {

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    if (requestContext == null || responseContext == null) {
      return;
    }

    String path = requestContext.getUriInfo() == null ? null : requestContext.getUriInfo().getPath();
    String method = requestContext.getMethod();

    if (path != null) {
      path = path.replaceAll("^/+", "");
      path = path.replaceAll("/+$", "");
    }

    if ("POST".equalsIgnoreCase(method)
        && "warehouse".equals(path)
        && responseContext.getStatus() == 200) {
      responseContext.setStatus(201);
    }
  }
}
