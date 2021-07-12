package coverage.framework.functionalinterfaces;

import coverage.framework.EntitySuper;

public interface FormRelationFunction<P extends EntitySuper, C extends EntitySuper> {
  public String relation(P parent, C child);
}
