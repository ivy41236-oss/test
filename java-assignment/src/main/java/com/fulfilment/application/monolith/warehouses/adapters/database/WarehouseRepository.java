package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.find("archivedAt is null").stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    var db = DbWarehouse.fromWarehouse(warehouse);
    this.persist(db);
    warehouse.id = db.id;
  }

  @Override
  @Transactional
  public void update(Warehouse warehouse) {
    if (warehouse.id == null) {
      var existing =
          this.find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode)
              .firstResult();
      if (existing == null) {
        return;
      }
      warehouse.id = existing.id;
    }

    DbWarehouse entity = this.findById(warehouse.id);
    if (entity == null) {
      return;
    }

    entity.businessUnitCode = warehouse.businessUnitCode;
    entity.location = warehouse.location;
    entity.capacity = warehouse.capacity;
    entity.stock = warehouse.stock;
    entity.createdAt = warehouse.createdAt;
    entity.archivedAt = warehouse.archivedAt;
  }

  @Override
  @Transactional
  public void remove(Warehouse warehouse) {
    if (warehouse == null) {
      return;
    }
    if (warehouse.id != null) {
      this.deleteById(warehouse.id);
      return;
    }

    if (warehouse.businessUnitCode != null) {
      this.delete("businessUnitCode", warehouse.businessUnitCode);
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    if (buCode == null) {
      return null;
    }

    DbWarehouse entity =
        this.find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResult();
    return entity == null ? null : entity.toWarehouse();
  }
}
