package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import org.junit.jupiter.api.Test;

public class WarehouseValidationTest {

  @Test
  void requireWarehouseData_null_throws() {
    assertThrows(IllegalArgumentException.class, () -> WarehouseValidation.requireWarehouseData(null));
  }

  @Test
  void requireBusinessUnitCode_blank_throws() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = " ";
    assertThrows(
        IllegalArgumentException.class, () -> WarehouseValidation.requireBusinessUnitCode(warehouse));
  }

  @Test
  void requireLocation_blank_throws() {
    Warehouse warehouse = validWarehouse();
    warehouse.location = " ";
    assertThrows(IllegalArgumentException.class, () -> WarehouseValidation.requireLocation(warehouse));
  }

  @Test
  void requireCapacityPositive_zero_throws() {
    Warehouse warehouse = validWarehouse();
    warehouse.capacity = 0;
    assertThrows(
        IllegalArgumentException.class, () -> WarehouseValidation.requireCapacityPositive(warehouse));
  }

  @Test
  void requireStockNonNegative_negative_throws() {
    Warehouse warehouse = validWarehouse();
    warehouse.stock = -1;
    assertThrows(
        IllegalArgumentException.class, () -> WarehouseValidation.requireStockNonNegative(warehouse));
  }

  @Test
  void requireStockNotExceedCapacity_exceeded_throws() {
    Warehouse warehouse = validWarehouse();
    warehouse.stock = 11;
    warehouse.capacity = 10;
    assertThrows(
        IllegalArgumentException.class,
        () -> WarehouseValidation.requireStockNotExceedCapacity(warehouse));
  }

  @Test
  void validateForCreateOrReplace_valid_passes() {
    assertDoesNotThrow(() -> WarehouseValidation.validateForCreateOrReplace(validWarehouse()));
  }

  private static Warehouse validWarehouse() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU-001";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 10;
    warehouse.stock = 5;
    return warehouse;
  }
}
