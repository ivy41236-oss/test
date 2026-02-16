package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import org.jboss.logging.Logger;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {
  private static final Logger LOGGER = Logger.getLogger(CreateWarehouseUseCase.class.getName());

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    WarehouseValidation.validateForCreateOrReplace(warehouse);
    LOGGER.debugf("create use case started for businessUnitCode=%s", warehouse.businessUnitCode);

    // Business Unit Code Verification
    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw new IllegalArgumentException("Business unit code already exists");
    }

    // Location Validation
    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw new IllegalArgumentException("Invalid location");
    }

    // Warehouse Creation Feasibility + Capacity constraints
    var activeWarehouses = warehouseStore.getAll();
    long activeCountAtLocation =
        activeWarehouses.stream().filter(w -> warehouse.location.equals(w.location)).count();
    if (activeCountAtLocation >= location.maxNumberOfWarehouses) {
      throw new IllegalArgumentException("Maximum number of warehouses reached for this location");
    }

    int activeCapacityAtLocation =
        activeWarehouses.stream()
            .filter(w -> warehouse.location.equals(w.location))
            .mapToInt(w -> w.capacity == null ? 0 : w.capacity)
            .sum();
    if (activeCapacityAtLocation + warehouse.capacity > location.maxCapacity) {
      throw new IllegalArgumentException("Location capacity limit exceeded");
    }

    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;

    // if all went well, create the warehouse
    warehouseStore.create(warehouse);
    LOGGER.debugf("create use case completed for businessUnitCode=%s", warehouse.businessUnitCode);
  }
}
