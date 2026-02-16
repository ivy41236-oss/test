package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.List;
import org.junit.jupiter.api.Test;

public class CreateWarehouseUseCaseTest {

  @Test
  void create_nullWarehouse_throws() {
    WarehouseStore store = mock(WarehouseStore.class);
    LocationResolver resolver = mock(LocationResolver.class);
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);

    assertThrows(IllegalArgumentException.class, () -> useCase.create(null));
    verify(store, never()).create(any());
  }

  @Test
  void create_stockGreaterThanCapacity_throws() {
    WarehouseStore store = mock(WarehouseStore.class);
    LocationResolver resolver = mock(LocationResolver.class);
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);
    Warehouse warehouse = validWarehouse();
    warehouse.stock = 11;
    warehouse.capacity = 10;

    assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    verify(store, never()).create(any());
  }

  @Test
  void create_duplicateBusinessUnitCode_throws() {
    WarehouseStore store = mock(WarehouseStore.class);
    LocationResolver resolver = mock(LocationResolver.class);
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);
    Warehouse warehouse = validWarehouse();
    when(store.findByBusinessUnitCode("BU-001")).thenReturn(new Warehouse());

    assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    verify(store, never()).create(any());
  }

  @Test
  void create_invalidLocation_throws() {
    WarehouseStore store = mock(WarehouseStore.class);
    LocationResolver resolver = mock(LocationResolver.class);
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);
    Warehouse warehouse = validWarehouse();
    when(store.findByBusinessUnitCode("BU-001")).thenReturn(null);
    when(resolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(null);

    assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    verify(store, never()).create(any());
  }

  @Test
  void create_maxWarehousesAtLocationExceeded_throws() {
    WarehouseStore store = mock(WarehouseStore.class);
    LocationResolver resolver = mock(LocationResolver.class);
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);
    Warehouse warehouse = validWarehouse();
    when(store.findByBusinessUnitCode("BU-001")).thenReturn(null);
    when(resolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 1, 100));
    when(store.getAll()).thenReturn(List.of(existingWarehouse("OTHER-1", "AMSTERDAM-001", 40, 5)));

    assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    verify(store, never()).create(any());
  }

  @Test
  void create_capacityLimitExceeded_throws() {
    WarehouseStore store = mock(WarehouseStore.class);
    LocationResolver resolver = mock(LocationResolver.class);
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);
    Warehouse warehouse = validWarehouse();
    warehouse.capacity = 30;
    when(store.findByBusinessUnitCode("BU-001")).thenReturn(null);
    when(resolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 5, 50));
    when(store.getAll()).thenReturn(List.of(existingWarehouse("OTHER-1", "AMSTERDAM-001", 25, 5)));

    assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    verify(store, never()).create(any());
  }

  @Test
  void create_success_setsAuditFieldsAndCallsStore() {
    WarehouseStore store = mock(WarehouseStore.class);
    LocationResolver resolver = mock(LocationResolver.class);
    CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(store, resolver);
    Warehouse warehouse = validWarehouse();
    when(store.findByBusinessUnitCode("BU-001")).thenReturn(null);
    when(resolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 5, 100));
    when(store.getAll()).thenReturn(List.of(existingWarehouse("OTHER-1", "AMSTERDAM-001", 20, 3)));

    useCase.create(warehouse);

    verify(store).create(warehouse);
    assertNotNull(warehouse.createdAt);
    assertNull(warehouse.archivedAt);
  }

  private static Warehouse validWarehouse() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU-001";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 40;
    warehouse.stock = 10;
    return warehouse;
  }

  private static Warehouse existingWarehouse(
      String businessUnitCode, String location, int capacity, int stock) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = businessUnitCode;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    return warehouse;
  }
}
