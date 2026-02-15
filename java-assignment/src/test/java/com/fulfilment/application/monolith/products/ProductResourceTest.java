package com.fulfilment.application.monolith.products;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class ProductResourceTest {

    @Inject
    ProductRepository productRepository;

    @Test
    void list() {
        given()
            .when().get("/product")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @Test
    @Transactional
    void createAndGet() {
        Integer idInt = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"CoverageTest\",\"description\":\"desc\",\"price\":12.34,\"stock\":5}")
            .when().post("/product")
            .then()
            .statusCode(201)
            .extract().path("id");
        Long id = idInt.longValue();

        given()
            .when().get("/product/" + id)
            .then()
            .statusCode(200)
            .body("name", equalTo("CoverageTest"))
            .body("description", equalTo("desc"))
            .body("price", equalTo(12.34f))
            .body("stock", equalTo(5));

        // cleanup
        productRepository.deleteById(id);
    }

    @Test
    void getSingleNotFound() {
        given()
            .when().get("/product/99999")
            .then()
            .statusCode(404);
    }

    @Test
    void createWithIdFails() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"id\":123,\"name\":\"Bad\"}")
            .when().post("/product")
            .then()
            .statusCode(422);
    }

    @Test
    void updateNotFound() {
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", 99999)
            .body("{\"name\":\"DoesNotExist\"}")
            .when().put("/product/{id}")
            .then()
            .statusCode(404);
    }

    @Test
    void deleteNotFound() {
        given()
            .pathParam("id", 99999)
            .when().delete("/product/{id}")
            .then()
            .statusCode(404);
    }
}
