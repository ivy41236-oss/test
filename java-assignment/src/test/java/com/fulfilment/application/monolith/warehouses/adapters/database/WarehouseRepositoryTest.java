package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

@QuarkusTest
class WarehouseRepositoryTest {

  @Inject WarehouseRepository repository;

  @Test
  void smoke() {
    assertNotNull(repository);
  }

  @Test
  @Transactional
  void createAndFindByBusinessUnitCode() {
    String buCode = "MWH.REPO.CREATE." + System.nanoTime();
    Warehouse warehouse = warehouse(buCode, "AMSTERDAM-001", 20, 3);

    repository.create(warehouse);

    assertNotNull(warehouse.id);
    Warehouse found = repository.findByBusinessUnitCode(buCode);
    assertNotNull(found);
    assertEquals(buCode, found.businessUnitCode);

    repository.remove(found);
  }

  @Test
  @Transactional
  void getAll_excludesArchivedWarehouses() {
    String activeCode = "MWH.REPO.ACTIVE." + System.nanoTime();
    String archivedCode = "MWH.REPO.ARCHIVED." + System.nanoTime();

    Warehouse active = warehouse(activeCode, "AMSTERDAM-001", 10, 1);
    Warehouse archived = warehouse(archivedCode, "AMSTERDAM-001", 10, 1);
    archived.archivedAt = LocalDateTime.now();
    repository.create(active);
    repository.create(archived);

    var all = repository.getAll();

    assertTrue(all.stream().anyMatch(w -> activeCode.equals(w.businessUnitCode)));
    assertTrue(all.stream().noneMatch(w -> archivedCode.equals(w.businessUnitCode)));

    repository.remove(active);
    repository.remove(archived);
  }

  @Test
  @Transactional
  void update_withId_updatesEntity() {
    String buCode = "MWH.REPO.UPDATE.ID." + System.nanoTime();
    Warehouse warehouse = warehouse(buCode, "AMSTERDAM-001", 10, 1);
    repository.create(warehouse);

    warehouse.location = "AMSTERDAM-002";
    warehouse.capacity = 30;
    warehouse.stock = 5;
    repository.update(warehouse);

    Warehouse found = repository.findByBusinessUnitCode(buCode);
    assertNotNull(found);
    assertEquals("AMSTERDAM-002", found.location);
    assertEquals(30, found.capacity);
    assertEquals(5, found.stock);

    repository.remove(found);
  }

  @Test
  @Transactional
  void update_withoutId_usesBusinessUnitCode() {
    String buCode = "MWH.REPO.UPDATE.BU." + System.nanoTime();
    Warehouse existing = warehouse(buCode, "AMSTERDAM-001", 10, 1);
    repository.create(existing);

    Warehouse update = warehouse(buCode, "TILBURG-001", 15, 2);
    update.id = null;
    repository.update(update);

    Warehouse found = repository.findByBusinessUnitCode(buCode);
    assertNotNull(found);
    assertEquals("TILBURG-001", found.location);
    assertEquals(15, found.capacity);
    assertEquals(2, found.stock);

    repository.remove(found);
  }

  @Test
  @Transactional
  void update_withoutId_andNoExisting_doesNothing() {
    Warehouse update = warehouse("MWH.REPO.NOTFOUND." + System.nanoTime(), "AMSTERDAM-001", 10, 1);
    update.id = null;

    assertDoesNotThrow(() -> repository.update(update));
  }

  @Test
  @Transactional
  void remove_null_doesNothing() {
    assertDoesNotThrow(() -> repository.remove(null));
  }

  @Test
  @Transactional
  void remove_byBusinessUnitCode_deletesEntity() {
    String buCode = "MWH.REPO.REMOVE.BU." + System.nanoTime();
    Warehouse warehouse = warehouse(buCode, "AMSTERDAM-001", 10, 1);
    repository.create(warehouse);

    Warehouse byBu = new Warehouse();
    byBu.businessUnitCode = buCode;
    repository.remove(byBu);

    Warehouse found = repository.findByBusinessUnitCode(buCode);
    assertNull(found);
  }

  @Test
  @Transactional
  void remove_withoutIdAndBusinessUnitCode_doesNothing() {
    Warehouse warehouse = new Warehouse();
    assertDoesNotThrow(() -> repository.remove(warehouse));
  }

  @Test
  void findByBusinessUnitCode_null_returnsNull() {
    assertNull(repository.findByBusinessUnitCode(null));
  }

  private static Warehouse warehouse(String buCode, String location, int capacity, int stock) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = buCode;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;
    return warehouse;
  }
}
