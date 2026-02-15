package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    if (warehouse == null) {
      throw new IllegalArgumentException("Warehouse data is required");
    }

    if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank()) {
      throw new IllegalArgumentException("Business unit code is required");
    }

    if (warehouse.location == null || warehouse.location.isBlank()) {
      throw new IllegalArgumentException("Location is required");
    }

    if (warehouse.capacity == null || warehouse.capacity <= 0) {
      throw new IllegalArgumentException("Capacity must be greater than 0");
    }

    if (warehouse.stock == null || warehouse.stock < 0) {
      throw new IllegalArgumentException("Stock must be 0 or greater");
    }

    if (warehouse.stock > warehouse.capacity) {
      throw new IllegalArgumentException("Stock cannot exceed capacity");
    }

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
    var activeWarehouses =
        warehouseStore.getAll().stream().filter(w -> w.archivedAt == null).toList();
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
  }
}
