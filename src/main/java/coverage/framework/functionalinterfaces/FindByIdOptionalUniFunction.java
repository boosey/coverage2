package coverage.framework.functionalinterfaces;

import coverage.framework.EntitySuper;
import io.smallrye.mutiny.Uni;
import java.util.Optional;
import org.bson.types.ObjectId;

public interface FindByIdOptionalUniFunction {
  public Uni<Optional<EntitySuper>> apply(ObjectId id);
}
