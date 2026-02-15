package com.fulfilment.application.monolith.products;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ProductRepositoryTest {

    @Inject
    ProductRepository productRepository;

    @Test
    void smoke() {
        assertNotNull(productRepository);
    }

    @Test
    @Transactional
    void persistAndFind() {
        Product p = new Product("TestRepo");
        productRepository.persist(p);
        assertNotNull(p.id);
        Product found = productRepository.findById(p.id);
        assertEquals("TestRepo", found.name);
    }
}
