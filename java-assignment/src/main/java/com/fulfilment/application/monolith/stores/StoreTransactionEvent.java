package com.fulfilment.application.monolith.stores;

public class StoreTransactionEvent {

  public enum Type {
    CREATED,
    UPDATED
  }

  public final Store store;
  public final Type type;

  public StoreTransactionEvent(Store store, Type type) {
    this.store = store;
    this.type = type;
  }
}
