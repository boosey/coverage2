package coverage;

import coverage.framework.EntityInterface;
import coverage.framework.EntitySuper;
import java.util.Set;

public class Engagement extends EntitySuper {

  public String name;
  public String description;
  public String engagementManagerId;
  public String engagementLeaderId;
  public Set<String> talentIds = Set.of();

  public <T extends EntityInterface> void updateFields(T updates) {
    Engagement e = (Engagement) updates;
    this.name = e.name;
    this.description = e.description;
    this.engagementManagerId = e.engagementManagerId;
    this.engagementLeaderId = e.engagementLeaderId;
    this.talentIds = Set.copyOf(e.talentIds);

    return;
  }

  public void assignTalent(String accountId) {
    talentIds.add(accountId);
  }

  public void unassignTalent(String accountId) {
    talentIds.remove(accountId);
  }
}
