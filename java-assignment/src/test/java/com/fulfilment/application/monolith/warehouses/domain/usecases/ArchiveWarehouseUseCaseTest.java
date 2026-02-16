package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.Test;

public class ArchiveWarehouseUseCaseTest {

  @Test
  void archive_nullWarehouse_throws() {
    WarehouseStore store = mock(WarehouseStore.class);
    ArchiveWarehouseUseCase useCase = new ArchiveWarehouseUseCase(store);

    assertThrows(IllegalArgumentException.class, () -> useCase.archive(null));
    verify(store, never()).update(any());
  }

  @Test
  void archive_missingBusinessUnitCode_throws() {
    WarehouseStore store = mock(WarehouseStore.class);
    ArchiveWarehouseUseCase useCase = new ArchiveWarehouseUseCase(store);
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = " ";

    assertThrows(IllegalArgumentException.class, () -> useCase.archive(warehouse));
    verify(store, never()).update(any());
  }

  @Test
  void archive_notFound_throws() {
    WarehouseStore store = mock(WarehouseStore.class);
    ArchiveWarehouseUseCase useCase = new ArchiveWarehouseUseCase(store);
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU-001";
    when(store.findByBusinessUnitCode("BU-001")).thenReturn(null);

    assertThrows(IllegalStateException.class, () -> useCase.archive(warehouse));
    verify(store, never()).update(any());
  }

  @Test
  void archive_alreadyArchived_throws() {
    WarehouseStore store = mock(WarehouseStore.class);
    ArchiveWarehouseUseCase useCase = new ArchiveWarehouseUseCase(store);
    Warehouse request = new Warehouse();
    request.businessUnitCode = "BU-001";
    Warehouse current = new Warehouse();
    current.businessUnitCode = "BU-001";
    current.archivedAt = java.time.LocalDateTime.now();
    when(store.findByBusinessUnitCode("BU-001")).thenReturn(current);

    assertThrows(IllegalStateException.class, () -> useCase.archive(request));
    verify(store, never()).update(any());
  }

  @Test
  void archive_success_setsArchivedAtAndUpdates() {
    WarehouseStore store = mock(WarehouseStore.class);
    ArchiveWarehouseUseCase useCase = new ArchiveWarehouseUseCase(store);
    Warehouse request = new Warehouse();
    request.businessUnitCode = "BU-001";
    Warehouse current = new Warehouse();
    current.businessUnitCode = "BU-001";
    current.archivedAt = null;
    when(store.findByBusinessUnitCode("BU-001")).thenReturn(current);

    useCase.archive(request);

    assertNotNull(current.archivedAt);
    verify(store).update(current);
  }
}
