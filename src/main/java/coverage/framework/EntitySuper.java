package coverage.framework;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;

public class EntitySuper
  extends ReactivePanacheMongoEntity
  implements EntityInterface {

  @Override
  public <T extends EntityInterface> void updateFields(T updates) {
    throw new UnsupportedOperationException(
      "A subclass must implement this method"
    );
  }
}
