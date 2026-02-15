package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ArchiveWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ReplaceWarehouseUseCase;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import org.jboss.resteasy.reactive.ResponseStatus;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject private WarehouseRepository warehouseRepository;
  @Inject private CreateWarehouseUseCase createWarehouseUseCase;
  @Inject private ReplaceWarehouseUseCase replaceWarehouseUseCase;
  @Inject private ArchiveWarehouseUseCase archiveWarehouseUseCase;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  @Transactional
  @ResponseStatus(201)
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    try {
      var domain = toDomainWarehouse(data);
      createWarehouseUseCase.create(domain);
      return toWarehouseResponse(domain);
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    Long numericId;
    try {
      numericId = Long.parseLong(id);
    } catch (Exception e) {
      throw new WebApplicationException("Invalid warehouse id", 400);
    }

    var entity = warehouseRepository.findById(numericId);
    if (entity == null) {
      throw new WebApplicationException("Warehouse unit not found", 404);
    }
    return toWarehouseResponse(entity.toWarehouse());
  }

  @Override
  @Transactional
  public void archiveAWarehouseUnitByID(String id) {
    Long numericId;
    try {
      numericId = Long.parseLong(id);
    } catch (Exception e) {
      throw new WebApplicationException("Invalid warehouse id", 400);
    }

    var entity = warehouseRepository.findById(numericId);
    if (entity == null) {
      throw new WebApplicationException("Warehouse unit not found", 404);
    }

    try {
      var domain = entity.toWarehouse();
      archiveWarehouseUseCase.archive(domain);
    } catch (IllegalStateException e) {
      throw new WebApplicationException(e.getMessage(), 404);
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  @Override
  @Transactional
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {
    try {
      var domain = toDomainWarehouse(data);
      domain.businessUnitCode = businessUnitCode;
      replaceWarehouseUseCase.replace(domain);
      return toWarehouseResponse(domain);
    } catch (IllegalStateException e) {
      throw new WebApplicationException(e.getMessage(), 404);
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    if (warehouse.id != null) {
      response.setId(String.valueOf(warehouse.id));
    }
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomainWarehouse(
      Warehouse data) {
    var domain = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    if (data.getId() != null && !data.getId().isBlank()) {
      try {
        domain.id = Long.parseLong(data.getId());
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid warehouse id");
      }
    }
    domain.businessUnitCode = data.getBusinessUnitCode();
    domain.location = data.getLocation();
    domain.capacity = data.getCapacity();
    domain.stock = data.getStock();
    return domain;
  }
}
