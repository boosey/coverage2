package coverage;

import coverage.framework.EntityInterface;
import coverage.framework.EntitySuper;
import coverage.framework.TalentRole;
import java.util.Set;

public class Talent extends EntitySuper {

  public String name;
  public String address;
  public String city;
  public String state;
  public String zip;
  public TalentRole role;
  public Set<String> accountIds = Set.of();

  public <T extends EntityInterface> void updateFields(T updates) {
    Talent a = (Talent) updates;
    this.name = a.name;
    this.address = a.address;
    this.city = a.city;
    this.state = a.state;
    this.zip = a.zip;
    this.role = a.role;
    this.accountIds = Set.copyOf(a.accountIds);
    return;
  }

  public void assignAccount(String accountId) {
    accountIds.add(accountId);
  }

  public void unassignAccount(String accountId) {
    accountIds.remove(accountId);
  }
}
