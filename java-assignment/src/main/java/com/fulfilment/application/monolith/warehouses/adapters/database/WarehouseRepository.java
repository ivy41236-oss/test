package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import org.jboss.logging.Logger;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {
  private static final Logger LOGGER = Logger.getLogger(WarehouseRepository.class.getName());

  @Override
  public List<Warehouse> getAll() {
    var warehouses = this.find("archivedAt is null").stream().map(DbWarehouse::toWarehouse).toList();
    LOGGER.debugf("getAll returned %d active warehouses", warehouses.size());
    return warehouses;
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    LOGGER.debugf("create requested for businessUnitCode=%s", warehouse.businessUnitCode);
    var db = DbWarehouse.fromWarehouse(warehouse);
    this.persist(db);
    warehouse.id = db.id;
    LOGGER.debugf("create completed with id=%d", warehouse.id);
  }

  @Override
  @Transactional
  public void update(Warehouse warehouse) {
    LOGGER.debugf(
        "update requested for id=%s businessUnitCode=%s", warehouse.id, warehouse.businessUnitCode);
    if (warehouse.id == null) {
      var existing =
          this.find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode)
              .firstResult();
      if (existing == null) {
        LOGGER.debugf(
            "update skipped - active warehouse not found for businessUnitCode=%s",
            warehouse.businessUnitCode);
        return;
      }
      warehouse.id = existing.id;
    }

    DbWarehouse entity = this.findById(warehouse.id);
    if (entity == null) {
      LOGGER.debugf("update skipped - warehouse not found for id=%d", warehouse.id);
      return;
    }

    entity.businessUnitCode = warehouse.businessUnitCode;
    entity.location = warehouse.location;
    entity.capacity = warehouse.capacity;
    entity.stock = warehouse.stock;
    entity.createdAt = warehouse.createdAt;
    entity.archivedAt = warehouse.archivedAt;
    LOGGER.debugf("update completed for id=%d", warehouse.id);
  }

  @Override
  @Transactional
  public void remove(Warehouse warehouse) {
    if (warehouse == null) {
      LOGGER.debug("remove skipped - warehouse is null");
      return;
    }
    if (warehouse.id != null) {
      this.deleteById(warehouse.id);
      LOGGER.debugf("remove completed by id=%d", warehouse.id);
      return;
    }

    if (warehouse.businessUnitCode != null) {
      long deleted = this.delete("businessUnitCode", warehouse.businessUnitCode);
      LOGGER.debugf(
          "remove completed by businessUnitCode=%s deleted=%d",
          warehouse.businessUnitCode,
          deleted);
      return;
    }

    LOGGER.debug("remove skipped - no id/businessUnitCode provided");
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    if (buCode == null) {
      LOGGER.debug("findByBusinessUnitCode skipped - buCode is null");
      return null;
    }

    DbWarehouse entity =
        this.find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResult();
    LOGGER.debugf("findByBusinessUnitCode buCode=%s found=%s", buCode, entity != null);
    return entity == null ? null : entity.toWarehouse();
  }
}
