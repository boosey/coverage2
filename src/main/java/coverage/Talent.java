package coverage;

import coverage.framework.EntityInterface;
import coverage.framework.EntitySuper;
import coverage.framework.TalentRole;
import java.util.ArrayList;
import java.util.List;

public class Talent extends EntitySuper {

  public String name;
  public String address;
  public String city;
  public String state;
  public String zip;
  public TalentRole role;
  public List<String> accountIds = new ArrayList<>();

  public <T extends EntityInterface> void updateFields(T updates) {
    Talent a = (Talent) updates;
    this.name = a.name;
    this.address = a.address;
    this.city = a.city;
    this.state = a.state;
    this.zip = a.zip;
    this.role = a.role;
    this.accountIds = List.copyOf(a.accountIds);
    return;
  }

  public void assignAccount(String accountId) {
    accountIds.add(accountId);
  }
}
