package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {
  private static final Logger LOGGER = Logger.getLogger(ReplaceWarehouseUseCase.class.getName());

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    WarehouseValidation.validateForCreateOrReplace(newWarehouse);
    LOGGER.debugf(
        "replace use case started for businessUnitCode=%s", newWarehouse.businessUnitCode);

    Warehouse current = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (current == null || current.archivedAt != null) {
      throw new IllegalStateException("Warehouse unit not found");
    }

    // Additional validations for replacing
    if (newWarehouse.capacity < current.stock) {
      throw new IllegalArgumentException(
          "New warehouse capacity must accommodate stock from previous warehouse");
    }

    if (newWarehouse.stock.intValue() != current.stock.intValue()) {
      throw new IllegalArgumentException("Stock must match the previous warehouse stock");
    }

    Location location = locationResolver.resolveByIdentifier(newWarehouse.location);
    if (location == null) {
      throw new IllegalArgumentException("Invalid location");
    }

    var activeWarehouses = warehouseStore.getAll();
    long activeCountAtLocationExcludingCurrent =
        activeWarehouses.stream()
            .filter(w -> newWarehouse.location.equals(w.location))
            .filter(w -> !newWarehouse.businessUnitCode.equals(w.businessUnitCode))
            .count();
    if (activeCountAtLocationExcludingCurrent >= location.maxNumberOfWarehouses) {
      throw new IllegalArgumentException("Maximum number of warehouses reached for this location");
    }

    int activeCapacityAtLocationExcludingCurrent =
        activeWarehouses.stream()
            .filter(w -> newWarehouse.location.equals(w.location))
            .filter(w -> !newWarehouse.businessUnitCode.equals(w.businessUnitCode))
            .mapToInt(w -> w.capacity == null ? 0 : w.capacity)
            .sum();
    if (activeCapacityAtLocationExcludingCurrent + newWarehouse.capacity > location.maxCapacity) {
      throw new IllegalArgumentException("Location capacity limit exceeded");
    }

    // archive current
    current.archivedAt = LocalDateTime.now();
    warehouseStore.update(current);

    // create new
    newWarehouse.id = null;
    newWarehouse.createdAt = LocalDateTime.now();
    newWarehouse.archivedAt = null;
    warehouseStore.create(newWarehouse);
    LOGGER.debugf(
        "replace use case completed; new warehouse created for businessUnitCode=%s",
        newWarehouse.businessUnitCode);
  }
}
