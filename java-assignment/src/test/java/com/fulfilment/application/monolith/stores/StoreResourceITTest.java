package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
public class StoreResourceITTest {

  @InjectMock LegacyStoreManagerGateway legacyStoreManagerGateway;

  @Test
  public void create_shouldInvokeLegacyOnlyAfterTransactionCommit() {
    Store store = new Store();
    store.name = "IT-STORE-1";
    store.quantityProductsInStock = 10;

    given()
        .contentType("application/json")
        .body(store)
        .when()
        .post("/store")
        .then()
        .statusCode(201)
        .body("id", notNullValue());

    Mockito.verify(legacyStoreManagerGateway, Mockito.timeout(2_000).times(1))
        .createStoreOnLegacySystem(Mockito.argThat(s -> s != null && "IT-STORE-1".equals(s.name)));
  }

  @Test
  public void update_shouldInvokeLegacyOnlyAfterTransactionCommit() {
    Store store = new Store();
    store.name = "IT-STORE-2";
    store.quantityProductsInStock = 1;

    Long id =
        given()
            .contentType("application/json")
            .body(store)
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

    Mockito.verify(legacyStoreManagerGateway, Mockito.timeout(2_000).times(1))
        .createStoreOnLegacySystem(Mockito.any());
    Mockito.reset(legacyStoreManagerGateway);

    Store updated = new Store();
    updated.name = "IT-STORE-2-UPDATED";
    updated.quantityProductsInStock = 2;

    given()
        .contentType("application/json")
        .body(updated)
        .when()
        .put("/store/" + id)
        .then()
        .statusCode(200);

    Mockito.verify(legacyStoreManagerGateway, Mockito.timeout(2_000).times(1))
        .updateStoreOnLegacySystem(
            Mockito.argThat(s -> s != null && "IT-STORE-2-UPDATED".equals(s.name)));
  }

  @Test
  public void patch_shouldInvokeLegacyOnlyAfterTransactionCommit() {
    Store store = new Store();
    store.name = "IT-STORE-3";
    store.quantityProductsInStock = 1;

    Long id =
        given()
            .contentType("application/json")
            .body(store)
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

    Mockito.verify(legacyStoreManagerGateway, Mockito.timeout(2_000).times(1))
        .createStoreOnLegacySystem(Mockito.any());
    Mockito.reset(legacyStoreManagerGateway);

    Store patched = new Store();
    patched.name = "IT-STORE-3-PATCHED";
    patched.quantityProductsInStock = 3;

    given()
        .contentType("application/json")
        .body(patched)
        .when()
        .patch("/store/" + id)
        .then()
        .statusCode(200);

    Mockito.verify(legacyStoreManagerGateway, Mockito.timeout(2_000).times(1))
        .updateStoreOnLegacySystem(
            Mockito.argThat(s -> s != null && "IT-STORE-3-PATCHED".equals(s.name)));
  }

  @Test
  public void create_validationError_shouldNotInvokeLegacy() {
    Store invalid = new Store();
    invalid.id = 123L;
    invalid.name = "IT-STORE-INVALID";

    given()
        .contentType("application/json")
        .body(invalid)
        .when()
        .post("/store")
        .then()
        .statusCode(422);

    Mockito.verifyNoInteractions(legacyStoreManagerGateway);
  }
}
