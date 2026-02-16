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

public class ReplaceWarehouseUseCaseTest {

  @Test
  void replace_nullWarehouse_throws() {
    WarehouseStore store = mock(WarehouseStore.class);
    LocationResolver resolver = mock(LocationResolver.class);
    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(store, resolver);

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(null));
    verify(store, never()).update(any());
    verify(store, never()).create(any());
  }

  @Test
  void replace_currentWarehouseNotFound_throws() {
    WarehouseStore store = mock(WarehouseStore.class);
    LocationResolver resolver = mock(LocationResolver.class);
    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(store, resolver);
    Warehouse replacement = replacementWarehouse();
    when(store.findByBusinessUnitCode("BU-001")).thenReturn(null);

    assertThrows(IllegalStateException.class, () -> useCase.replace(replacement));
    verify(store, never()).update(any());
    verify(store, never()).create(any());
  }

  @Test
  void replace_capacityLessThanCurrentStock_throws() {
    WarehouseStore store = mock(WarehouseStore.class);
    LocationResolver resolver = mock(LocationResolver.class);
    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(store, resolver);
    Warehouse replacement = replacementWarehouse();
    replacement.capacity = 3;
    Warehouse current = activeCurrentWarehouse();
    current.stock = 5;
    when(store.findByBusinessUnitCode("BU-001")).thenReturn(current);

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
    verify(store, never()).update(any());
    verify(store, never()).create(any());
  }

  @Test
  void replace_stockMismatch_throws() {
    WarehouseStore store = mock(WarehouseStore.class);
    LocationResolver resolver = mock(LocationResolver.class);
    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(store, resolver);
    Warehouse replacement = replacementWarehouse();
    replacement.stock = 6;
    Warehouse current = activeCurrentWarehouse();
    current.stock = 5;
    when(store.findByBusinessUnitCode("BU-001")).thenReturn(current);

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
    verify(store, never()).update(any());
    verify(store, never()).create(any());
  }

  @Test
  void replace_invalidLocation_throws() {
    WarehouseStore store = mock(WarehouseStore.class);
    LocationResolver resolver = mock(LocationResolver.class);
    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(store, resolver);
    Warehouse replacement = replacementWarehouse();
    Warehouse current = activeCurrentWarehouse();
    when(store.findByBusinessUnitCode("BU-001")).thenReturn(current);
    when(resolver.resolveByIdentifier("AMSTERDAM-002")).thenReturn(null);

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
    verify(store, never()).update(any());
    verify(store, never()).create(any());
  }

  @Test
  void replace_maxWarehousesAtLocationExceeded_throws() {
    WarehouseStore store = mock(WarehouseStore.class);
    LocationResolver resolver = mock(LocationResolver.class);
    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(store, resolver);
    Warehouse replacement = replacementWarehouse();
    Warehouse current = activeCurrentWarehouse();
    when(store.findByBusinessUnitCode("BU-001")).thenReturn(current);
    when(resolver.resolveByIdentifier("AMSTERDAM-002"))
        .thenReturn(new Location("AMSTERDAM-002", 1, 200));
    when(store.getAll()).thenReturn(List.of(existingWarehouse("OTHER-1", "AMSTERDAM-002", 30, 2)));

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
    verify(store, never()).update(any());
    verify(store, never()).create(any());
  }

  @Test
  void replace_capacityLimitExceeded_throws() {
    WarehouseStore store = mock(WarehouseStore.class);
    LocationResolver resolver = mock(LocationResolver.class);
    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(store, resolver);
    Warehouse replacement = replacementWarehouse();
    replacement.capacity = 40;
    Warehouse current = activeCurrentWarehouse();
    when(store.findByBusinessUnitCode("BU-001")).thenReturn(current);
    when(resolver.resolveByIdentifier("AMSTERDAM-002"))
        .thenReturn(new Location("AMSTERDAM-002", 5, 50));
    when(store.getAll()).thenReturn(List.of(existingWarehouse("OTHER-1", "AMSTERDAM-002", 20, 2)));

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
    verify(store, never()).update(any());
    verify(store, never()).create(any());
  }

  @Test
  void replace_success_archivesCurrentAndCreatesReplacement() {
    WarehouseStore store = mock(WarehouseStore.class);
    LocationResolver resolver = mock(LocationResolver.class);
    ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(store, resolver);
    Warehouse replacement = replacementWarehouse();
    Warehouse current = activeCurrentWarehouse();
    when(store.findByBusinessUnitCode("BU-001")).thenReturn(current);
    when(resolver.resolveByIdentifier("AMSTERDAM-002"))
        .thenReturn(new Location("AMSTERDAM-002", 5, 100));
    when(store.getAll()).thenReturn(List.of(existingWarehouse("OTHER-1", "AMSTERDAM-002", 20, 2)));

    useCase.replace(replacement);

    verify(store).update(current);
    verify(store).create(replacement);
    assertNotNull(current.archivedAt);
    assertNull(replacement.id);
    assertNotNull(replacement.createdAt);
    assertNull(replacement.archivedAt);
  }

  private static Warehouse replacementWarehouse() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU-001";
    warehouse.location = "AMSTERDAM-002";
    warehouse.capacity = 30;
    warehouse.stock = 5;
    return warehouse;
  }

  private static Warehouse activeCurrentWarehouse() {
    Warehouse warehouse = new Warehouse();
    warehouse.id = 1L;
    warehouse.businessUnitCode = "BU-001";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 20;
    warehouse.stock = 5;
    warehouse.archivedAt = null;
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
