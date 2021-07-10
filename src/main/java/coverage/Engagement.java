package coverage;

import coverage.framework.EntityInterface;
import coverage.framework.EntitySuper;
import java.util.Set;

public class Engagement extends EntitySuper {

  public String name;
  public String description;
  public String engagementManagerId;
  public String engagementLeaderId;
  public Set<String> talent = Set.of();

  public <T extends EntityInterface> void updateFields(T updates) {
    Engagement e = (Engagement) updates;
    this.name = e.name;

    this.engagementManagerId = e.engagementManagerId;
    this.engagementLeaderId = e.engagementLeaderId;
    this.talent = Set.copyOf(e.talent);

    return;
  }
}
