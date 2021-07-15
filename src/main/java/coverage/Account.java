package coverage;

import coverage.framework.EntityInterface;
import coverage.framework.EntitySuper;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Account extends EntitySuper {

  final Logger logger = LoggerFactory.getLogger(Account.class);

  public String name;
  public String address;
  public String city;
  public String state;
  public String zip;

  @BsonProperty("squadmanagerid")
  public String squadManagerId;

  @BsonProperty("designmanagerid")
  public String designManagerId;

  @BsonProperty("btcmanagerid")
  public String btcManagerId;

  public String ctl;
  public String market;

  @BsonProperty("ctlmanager")
  public String ctlManager;

  public String icl;
  public String vp;

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
    this.ctl = a.ctl;
    this.market = a.market;
    this.ctlManager = a.ctlManager;
    this.vp = a.vp;
    this.icl = a.icl;
    return;
  }
}
