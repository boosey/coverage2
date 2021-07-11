package coverage;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "config")
public interface Configuration {
  Event event();

  public interface Event {
    String accountBtcManagerAssigned();
    String accountBtcManagerUnassigned();
    String accountDesignManagerAssigned();
    String accountDesignManagerUnassigned();
    String accountSquadManagerAssigned();
    String accountSquadManagerUnassigned();
    String accountEngagementAssigned();
    String accountEngagementUnassigned();

    String talentManagerAssigned();
    String talentManagerUnassigned();
    String talentAccountAssigned();
    String talentAccountUnassigned();
    String talentEngagementAssigned();
    String talentEngagementUnassigned();

    String engagementManagerAssigned();
    String engagementManagerUnassigned();
    String engagementLeaderAssigned();
    String engagementLeaderUnassigned();
    String engagementTalentAssigned();
    String engagementTalentUnassigned();
    String engagementAccountAssigned();
    String engagementAccountUnassigned();

    String accountUpdated();
    String accountCreated();
    String accountDeleted();

    String talentUpdated();
    String talentCreated();
    String talentDeleted();

    String engagementUpdated();
    String engagementCreated();
    String engagementDeleted();

    Property property();

    public interface Property {
      String name();
      String parentId();
      String childId();
      String accountId();
      String talentId();
      String engagementId();
    }
  }
}
