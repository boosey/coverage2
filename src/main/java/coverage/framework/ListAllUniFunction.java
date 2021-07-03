package coverage.framework;

import io.smallrye.mutiny.Uni;
import java.util.List;

@FunctionalInterface
public interface ListAllUniFunction {
  Uni<List<EntitySuper>> apply();
}
