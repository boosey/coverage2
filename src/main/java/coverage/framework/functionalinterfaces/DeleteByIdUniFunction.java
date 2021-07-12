package coverage.framework.functionalinterfaces;

import io.smallrye.mutiny.Uni;
import org.bson.types.ObjectId;

public interface DeleteByIdUniFunction {
  Uni<Boolean> apply(ObjectId id);
}
