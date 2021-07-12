package coverage.framework.functionalinterfaces;

import io.smallrye.mutiny.Uni;

@FunctionalInterface
public interface DeleteAllUniFunction {
  Uni<Long> apply();
}
