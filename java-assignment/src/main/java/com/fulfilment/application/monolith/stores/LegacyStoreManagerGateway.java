package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jboss.logging.Logger;

@ApplicationScoped
public class LegacyStoreManagerGateway {
  private static final Logger LOGGER = Logger.getLogger(LegacyStoreManagerGateway.class.getName());

  public void createStoreOnLegacySystem(Store store) {
    // just to emulate as this would send this to a legacy system, let's write a temp file with the
    writeToFile(store);
  }

  public void updateStoreOnLegacySystem(Store store) {
    // just to emulate as this would send this to a legacy system, let's write a temp file with the
    writeToFile(store);
  }

  private void writeToFile(Store store) {
    try {
      Path tempFile;

      tempFile = Files.createTempFile(store.name, ".txt");

      String content =
          "Store created. [ name ="
              + store.name
              + " ] [ items on stock ="
              + store.quantityProductsInStock
              + "]";
      Files.write(tempFile, content.getBytes());

      Files.delete(tempFile);

    } catch (Exception e) {
      LOGGER.error("Legacy sync call failed while persisting temporary payload", e);
      throw new RuntimeException("Legacy store synchronization failed", e);
    }
  }
}
