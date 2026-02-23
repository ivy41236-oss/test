package com.fulfilment.application.monolith.stores;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.Instant;
import java.util.List;

@Entity
public class StoreSyncRetryJob extends PanacheEntity {

  public enum Status {
    PENDING,
    PROCESSING,
    FAILED,
    SUCCEEDED,
    DEAD_LETTER
  }

  public Long storeId;

  @Column(length = 40)
  public String storeName;

  public int quantityProductsInStock;

  @Enumerated(EnumType.STRING)
  @Column(length = 20, nullable = false)
  public StoreTransactionEvent.Type eventType;

  @Enumerated(EnumType.STRING)
  @Column(length = 20, nullable = false)
  public Status status;

  public int attemptCount;

  @Column(length = 1024)
  public String lastError;

  public Instant nextAttemptAt;
  public Instant createdAt;
  public Instant updatedAt;

  public static List<StoreSyncRetryJob> findDue(int batchSize, Instant now) {
    return find(
            "status in (?1, ?2) and nextAttemptAt <= ?3 order by createdAt asc",
            Status.PENDING,
            Status.FAILED,
            now)
        .page(0, batchSize)
        .list();
  }
}
