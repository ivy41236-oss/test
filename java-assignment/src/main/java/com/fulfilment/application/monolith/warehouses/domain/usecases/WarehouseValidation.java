package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;

public final class WarehouseValidation {

  private WarehouseValidation() {}

  public static void requireWarehouseData(Warehouse warehouse) {
    if (warehouse == null) {
      throw new IllegalArgumentException("Warehouse data is required");
    }
  }

  public static void requireBusinessUnitCode(Warehouse warehouse) {
    if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank()) {
      throw new IllegalArgumentException("Business unit code is required");
    }
  }

  public static void requireLocation(Warehouse warehouse) {
    if (warehouse.location == null || warehouse.location.isBlank()) {
      throw new IllegalArgumentException("Location is required");
    }
  }

  public static void requireCapacityPositive(Warehouse warehouse) {
    if (warehouse.capacity == null || warehouse.capacity <= 0) {
      throw new IllegalArgumentException("Capacity must be greater than 0");
    }
  }

  public static void requireStockNonNegative(Warehouse warehouse) {
    if (warehouse.stock == null || warehouse.stock < 0) {
      throw new IllegalArgumentException("Stock must be 0 or greater");
    }
  }

  public static void requireStockNotExceedCapacity(Warehouse warehouse) {
    if (warehouse.stock > warehouse.capacity) {
      throw new IllegalArgumentException("Stock cannot exceed capacity");
    }
  }

  public static void validateForCreateOrReplace(Warehouse warehouse) {
    requireWarehouseData(warehouse);
    requireBusinessUnitCode(warehouse);
    requireLocation(warehouse);
    requireCapacityPositive(warehouse);
    requireStockNonNegative(warehouse);
    requireStockNotExceedCapacity(warehouse);
  }
}
