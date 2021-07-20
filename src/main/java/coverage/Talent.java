package coverage;

import coverage.framework.EntityInterface;
import coverage.framework.EntitySuper;
import java.util.Set;
import org.bson.codecs.pojo.annotations.BsonProperty;

public class Talent extends EntitySuper {

  @BsonProperty("firstname")
  public String firstName;

  @BsonProperty("lastname")
  public String lastName;

  public String address;
  public String city;
  public String state;
  public String zip;
  public String country;
  // public TalentRole role;

  @BsonProperty("countrycode")
  public String countryCode;

  public String serial;

  @BsonProperty("managerid")
  public String managerId;

  @BsonProperty("accountids")
  public Set<String> accountIds = Set.of();

  public <T extends EntityInterface> void updateFields(T updates) {
    Talent a = (Talent) updates;
    this.firstName = a.firstName;
    this.lastName = a.lastName;
    this.address = a.address;
    this.city = a.city;
    this.state = a.state;
    this.zip = a.zip;
    this.country = a.country;
    this.managerId = a.managerId;
    this.accountIds = Set.copyOf(a.accountIds);
    // this.role = a.role;
    this.countryCode = a.countryCode;
    this.serial = a.serial;
    return;
  }

  public void assignAccount(String accountId) {
    accountIds.add(accountId);
  }

  public void unassignAccount(String accountId) {
    accountIds.remove(accountId);
  }
}
