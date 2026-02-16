package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ArchiveWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ReplaceWarehouseUseCase;
import jakarta.ws.rs.WebApplicationException;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WarehouseResourceImplTest {

  private WarehouseRepository warehouseRepository;
  private CreateWarehouseUseCase createWarehouseUseCase;
  private ReplaceWarehouseUseCase replaceWarehouseUseCase;
  private ArchiveWarehouseUseCase archiveWarehouseUseCase;
  private WarehouseResourceImpl resource;

  @BeforeEach
  void setUp() throws Exception {
    warehouseRepository = mock(WarehouseRepository.class);
    createWarehouseUseCase = mock(CreateWarehouseUseCase.class);
    replaceWarehouseUseCase = mock(ReplaceWarehouseUseCase.class);
    archiveWarehouseUseCase = mock(ArchiveWarehouseUseCase.class);
    resource = new WarehouseResourceImpl();

    setField(resource, "warehouseRepository", warehouseRepository);
    setField(resource, "createWarehouseUseCase", createWarehouseUseCase);
    setField(resource, "replaceWarehouseUseCase", replaceWarehouseUseCase);
    setField(resource, "archiveWarehouseUseCase", archiveWarehouseUseCase);
  }

  @Test
  void listAllWarehousesUnits_mapsResponse() {
    var domain = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    domain.id = 1L;
    domain.businessUnitCode = "BU-1";
    domain.location = "AMSTERDAM-001";
    domain.capacity = 20;
    domain.stock = 5;
    when(warehouseRepository.getAll()).thenReturn(List.of(domain));

    List<com.warehouse.api.beans.Warehouse> response = resource.listAllWarehousesUnits();

    assertEquals(1, response.size());
    assertEquals("1", response.get(0).getId());
    assertEquals("BU-1", response.get(0).getBusinessUnitCode());
  }

  @Test
  void createANewWarehouseUnit_success() {
    com.warehouse.api.beans.Warehouse request = new com.warehouse.api.beans.Warehouse();
    request.setBusinessUnitCode("BU-1");
    request.setLocation("AMSTERDAM-001");
    request.setCapacity(20);
    request.setStock(5);
    doNothing().when(createWarehouseUseCase).create(any());

    com.warehouse.api.beans.Warehouse response = resource.createANewWarehouseUnit(request);

    assertEquals("BU-1", response.getBusinessUnitCode());
    assertEquals("AMSTERDAM-001", response.getLocation());
    verify(createWarehouseUseCase).create(any());
  }

  @Test
  void createANewWarehouseUnit_invalidWarehouseId_throws400() {
    com.warehouse.api.beans.Warehouse request = new com.warehouse.api.beans.Warehouse();
    request.setId("bad-id");
    request.setBusinessUnitCode("BU-1");
    request.setLocation("AMSTERDAM-001");
    request.setCapacity(20);
    request.setStock(5);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.createANewWarehouseUnit(request));
    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  void createANewWarehouseUnit_useCaseIllegalArgument_throws400() {
    com.warehouse.api.beans.Warehouse request = new com.warehouse.api.beans.Warehouse();
    request.setBusinessUnitCode("BU-1");
    request.setLocation("AMSTERDAM-001");
    request.setCapacity(20);
    request.setStock(5);
    doThrow(new IllegalArgumentException("bad request")).when(createWarehouseUseCase).create(any());

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.createANewWarehouseUnit(request));
    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  void getAWarehouseUnitByID_invalidId_throws400() {
    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.getAWarehouseUnitByID("x"));
    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  void getAWarehouseUnitByID_notFound_throws404() {
    when(warehouseRepository.findById(10L)).thenReturn(null);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.getAWarehouseUnitByID("10"));
    assertEquals(404, ex.getResponse().getStatus());
  }

  @Test
  void getAWarehouseUnitByID_success() {
    DbWarehouse entity = new DbWarehouse();
    entity.id = 10L;
    entity.businessUnitCode = "BU-10";
    entity.location = "AMSTERDAM-001";
    entity.capacity = 30;
    entity.stock = 8;
    when(warehouseRepository.findById(10L)).thenReturn(entity);

    com.warehouse.api.beans.Warehouse response = resource.getAWarehouseUnitByID("10");

    assertEquals("10", response.getId());
    assertEquals("BU-10", response.getBusinessUnitCode());
    assertNotNull(response.getCapacity());
  }

  @Test
  void archiveAWarehouseUnitByID_invalidId_throws400() {
    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("x"));
    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  void archiveAWarehouseUnitByID_notFound_throws404() {
    when(warehouseRepository.findById(1L)).thenReturn(null);

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("1"));
    assertEquals(404, ex.getResponse().getStatus());
  }

  @Test
  void archiveAWarehouseUnitByID_illegalState_throws404() {
    DbWarehouse entity = new DbWarehouse();
    entity.id = 1L;
    entity.businessUnitCode = "BU-1";
    when(warehouseRepository.findById(1L)).thenReturn(entity);
    doThrow(new IllegalStateException("not found")).when(archiveWarehouseUseCase).archive(any());

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("1"));
    assertEquals(404, ex.getResponse().getStatus());
  }

  @Test
  void archiveAWarehouseUnitByID_illegalArgument_throws400() {
    DbWarehouse entity = new DbWarehouse();
    entity.id = 1L;
    entity.businessUnitCode = "BU-1";
    when(warehouseRepository.findById(1L)).thenReturn(entity);
    doThrow(new IllegalArgumentException("bad request")).when(archiveWarehouseUseCase).archive(any());

    WebApplicationException ex =
        assertThrows(WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("1"));
    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  void archiveAWarehouseUnitByID_success() {
    DbWarehouse entity = new DbWarehouse();
    entity.id = 1L;
    entity.businessUnitCode = "BU-1";
    when(warehouseRepository.findById(1L)).thenReturn(entity);
    doNothing().when(archiveWarehouseUseCase).archive(any());

    resource.archiveAWarehouseUnitByID("1");

    verify(archiveWarehouseUseCase).archive(any());
  }

  @Test
  void replaceTheCurrentActiveWarehouse_illegalState_throws404() {
    com.warehouse.api.beans.Warehouse request = new com.warehouse.api.beans.Warehouse();
    request.setLocation("AMSTERDAM-001");
    request.setCapacity(20);
    request.setStock(5);
    doThrow(new IllegalStateException("not found")).when(replaceWarehouseUseCase).replace(any());

    WebApplicationException ex =
        assertThrows(
            WebApplicationException.class,
            () -> resource.replaceTheCurrentActiveWarehouse("BU-1", request));
    assertEquals(404, ex.getResponse().getStatus());
  }

  @Test
  void replaceTheCurrentActiveWarehouse_illegalArgument_throws400() {
    com.warehouse.api.beans.Warehouse request = new com.warehouse.api.beans.Warehouse();
    request.setLocation("AMSTERDAM-001");
    request.setCapacity(20);
    request.setStock(5);
    doThrow(new IllegalArgumentException("bad request")).when(replaceWarehouseUseCase).replace(any());

    WebApplicationException ex =
        assertThrows(
            WebApplicationException.class,
            () -> resource.replaceTheCurrentActiveWarehouse("BU-1", request));
    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  void replaceTheCurrentActiveWarehouse_success() {
    com.warehouse.api.beans.Warehouse request = new com.warehouse.api.beans.Warehouse();
    request.setId("11");
    request.setLocation("AMSTERDAM-001");
    request.setCapacity(20);
    request.setStock(5);
    doNothing().when(replaceWarehouseUseCase).replace(any());

    com.warehouse.api.beans.Warehouse response =
        resource.replaceTheCurrentActiveWarehouse("BU-1", request);

    assertEquals("11", response.getId());
    assertEquals("BU-1", response.getBusinessUnitCode());
    verify(replaceWarehouseUseCase).replace(any());
  }

  private static void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}
