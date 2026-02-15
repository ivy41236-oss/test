package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void archive(Warehouse warehouse) {
    if (warehouse == null) {
      throw new IllegalArgumentException("Warehouse data is required");
    }
    if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank()) {
      throw new IllegalArgumentException("Business unit code is required");
    }

    Warehouse current = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (current == null || current.archivedAt != null) {
      throw new IllegalStateException("Warehouse unit not found");
    }

    current.archivedAt = LocalDateTime.now();
    warehouseStore.update(current);
  }
}
