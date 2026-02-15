package com.fulfilment.application.monolith.warehouses;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class WarehouseResourceITTest {

  @Test
  public void list_shouldReturnInitialWarehouses() {
    given()
        .when()
        .get("/warehouse")
        .then()
        .statusCode(200)
        .body(containsString("MWH.001"), containsString("MWH.012"), containsString("MWH.023"));
  }

  @Test
  public void create_shouldReturn201AndPersist() {
    String buCode = "MWH.IT.100";

    String body =
        "{\n"
            + "  \"businessUnitCode\": \""
            + buCode
            + "\",\n"
            + "  \"location\": \"AMSTERDAM-001\",\n"
            + "  \"capacity\": 40,\n"
            + "  \"stock\": 10\n"
            + "}";

    given()
        .contentType("application/json")
        .body(body)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(201)
        .body("id", notNullValue())
        .body("businessUnitCode", containsString(buCode));
  }

  @Test
  public void create_invalidLocation_shouldReturn400() {
    String body =
        "{\n"
            + "  \"businessUnitCode\": \"MWH.IT.BAD.LOC\",\n"
            + "  \"location\": \"DOES-NOT-EXIST\",\n"
            + "  \"capacity\": 10,\n"
            + "  \"stock\": 1\n"
            + "}";

    given().contentType("application/json").body(body).when().post("/warehouse").then().statusCode(400);
  }

  @Test
  public void create_maxWarehousesAtLocation_shouldReturn400() {
    // ZWOLLE-001 maxNumberOfWarehouses = 1 and import.sql already has MWH.001 there
    String body =
        "{\n"
            + "  \"businessUnitCode\": \"MWH.IT.ZWOLLE.2\",\n"
            + "  \"location\": \"ZWOLLE-001\",\n"
            + "  \"capacity\": 10,\n"
            + "  \"stock\": 1\n"
            + "}";

    given().contentType("application/json").body(body).when().post("/warehouse").then().statusCode(400);
  }

  @Test
  public void replace_shouldArchiveOldAndCreateNew() {
    String body =
        "{\n"
            + "  \"location\": \"AMSTERDAM-002\",\n"
            + "  \"capacity\": 60,\n"
            + "  \"stock\": 5\n"
            + "}";

    given()
        .contentType("application/json")
        .body(body)
        .when()
        .post("/warehouse/MWH.012/replacement")
        .then()
        .statusCode(200)
        .body("id", notNullValue())
        .body("businessUnitCode", containsString("MWH.012"))
        .body("location", containsString("AMSTERDAM-002"));
  }

  @Test
  public void archive_shouldReturn204() {
    String buCode = "MWH.IT.ARCHIVE";

    String createBody =
        "{\n"
            + "  \"businessUnitCode\": \""
            + buCode
            + "\",\n"
            + "  \"location\": \"AMSTERDAM-001\",\n"
            + "  \"capacity\": 10,\n"
            + "  \"stock\": 1\n"
            + "}";

    given().contentType("application/json").body(createBody).when().post("/warehouse").then().statusCode(201);

    String id = findWarehouseIdByBusinessUnitCode(buCode);
    assertNotNull(id);

    given().when().delete("/warehouse/" + id).then().statusCode(204);
  }

  private String findWarehouseIdByBusinessUnitCode(String buCode) {
    List<String> ids =
        given()
            .when()
            .get("/warehouse")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getList("findAll { it.businessUnitCode == '" + buCode + "' }.id");

    if (ids == null || ids.isEmpty()) {
      return null;
    }
    return ids.get(0);
  }
}
