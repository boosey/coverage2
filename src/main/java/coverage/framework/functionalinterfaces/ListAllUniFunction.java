package coverage.framework.functionalinterfaces;

import coverage.framework.EntitySuper;
import io.smallrye.mutiny.Uni;
import java.util.List;

@FunctionalInterface
public interface ListAllUniFunction {
  Uni<List<EntitySuper>> apply();
}
