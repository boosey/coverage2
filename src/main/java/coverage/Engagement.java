package coverage;

import coverage.framework.EntityInterface;
import coverage.framework.EntitySuper;
import java.util.Set;
import org.bson.codecs.pojo.annotations.BsonProperty;

public class Engagement extends EntitySuper {

  public String name;
  public String description;

  @BsonProperty("engagementmanagerid")
  public String engagementManagerId;

  @BsonProperty("engagementleaderid")
  public String engagementLeaderId;

  @BsonProperty("talentids")
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
