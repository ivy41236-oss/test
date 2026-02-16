package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {
  private static final Logger LOGGER = Logger.getLogger(ArchiveWarehouseUseCase.class.getName());

  private final WarehouseStore warehouseStore;

  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void archive(Warehouse warehouse) {
    WarehouseValidation.requireWarehouseData(warehouse);
    LOGGER.debugf(
        "archive use case started for businessUnitCode=%s", warehouse.businessUnitCode);
    WarehouseValidation.requireBusinessUnitCode(warehouse);

    Warehouse current = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (current == null || current.archivedAt != null) {
      throw new IllegalStateException("Warehouse unit not found");
    }

    current.archivedAt = LocalDateTime.now();
    warehouseStore.update(current);
    LOGGER.debugf(
        "archive use case completed for businessUnitCode=%s", warehouse.businessUnitCode);
  }
}
