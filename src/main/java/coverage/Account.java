package coverage;

import coverage.framework.EntityInterface;
import coverage.framework.EntitySuper;

public class Account extends EntitySuper {

  public String name;
  public String address;
  public String city;
  public String state;
  public String zip;
  public String squadManagerId;
  public String designManagerId;
  public String btcManagerId;

  public <T extends EntityInterface> void updateFields(T updates) {
    Account a = (Account) updates;
    this.name = a.name;
    this.address = a.address;
    this.city = a.city;
    this.state = a.state;
    this.zip = a.zip;
    this.squadManagerId = a.squadManagerId;
    this.designManagerId = a.designManagerId;
    this.btcManagerId = a.btcManagerId;
    return;
  }
}
