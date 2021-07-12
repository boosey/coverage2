package coverage;

import coverage.framework.FormRelationFunction;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import io.vertx.core.json.JsonObject;
import java.util.Set;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

public class TalentEventProcessing {

  @Inject
  Logger log;

  @Inject
  Configuration config;

  @Incoming("account-event")
  public Uni<Void> consumeAccountEvent(
    KafkaRecord<JsonObject, JsonObject> msg
  ) {
    FormRelationFunction assign = (talent, account) -> {
      Talent t = (Talent) talent;
      Account a = (Account) account;
      t.assignAccount(a.id.toString());
      return null;
    };

    FormRelationFunction unassign = (talent, account) -> {
      Talent t = (Talent) talent;
      Account a = (Account) account;
      t.unassignAccount(a.id.toString());
      return null;
    };

    log.info("processing account-event: " + msg.getTopic());
    JsonObject p = msg.getPayload();
    String event = p.getString(config.event().property().name());
    Set<String> talentAssignedEvents = Set.of(
      config.event().accountBtcManagerAssigned(),
      config.event().accountSquadManagerAssigned(),
      config.event().accountDesignManagerAssigned()
    );
    Set<String> talentUnassignedEvents = Set.of(
      config.event().accountBtcManagerUnassigned(),
      config.event().accountSquadManagerUnassigned(),
      config.event().accountDesignManagerUnassigned()
    );

    if (talentAssignedEvents.contains(event)) {
      return processAccountTalentRelationshipChanged(msg, assign);
    } else if (talentUnassignedEvents.contains(event)) {
      return processAccountTalentRelationshipChanged(msg, unassign);
    }

    // if (event.equals(config.event().accountBtcManagerAssigned())) {
    //   return processAccountTalentRelationshipChanged(msg, assign);
    // } else if (event.equals(config.event().accountBtcManagerAssigned())) {
    //   return processAccountTalentRelationshipChanged(msg, unassign);
    // } else if (event.equals(config.event().accountDesignManagerAssigned())) {
    //   return processAccountTalentRelationshipChanged(msg, assign);
    // } else if (event.equals(config.event().accountDesignManagerUnassigned())) {
    //   return processAccountTalentRelationshipChanged(msg, unassign);
    // } else if (event.equals(config.event().accountSquadManagerAssigned())) {
    //   return processAccountTalentRelationshipChanged(msg, assign);
    // } else if (event.equals(config.event().accountSquadManagerUnassigned())) {
    //   return processAccountTalentRelationshipChanged(msg, unassign);
    // }

    // Put in dead letter queue
    return Uni.createFrom().completionStage(msg.ack());
  }

  public Uni<Void> processAccountTalentRelationshipChanged(
    KafkaRecord<JsonObject, JsonObject> msg,
    FormRelationFunction change
  ) {
    JsonObject p = msg.getPayload();

    return Uni
      .combine()
      .all()
      .unis(
        Talent.<Talent>findByIdOptional(
          new ObjectId(p.getString(config.event().property().talentId()))
        ),
        Account.<Account>findByIdOptional(
          new ObjectId(p.getString(config.event().property().parentId()))
        )
      )
      .asTuple()
      .onItem()
      .transform(
        tuple -> {
          return tuple.mapItem1(
            talentOpt -> {
              if (talentOpt.isPresent()) {
                return talentOpt.get();
              } else {
                throw new NotFoundException("Talent not found");
              }
            }
          );
        }
      )
      .onItem()
      .transform(
        tuple -> {
          return tuple.mapItem2(
            accountOpt -> {
              if (accountOpt.isPresent()) {
                return accountOpt.get();
              } else {
                throw new NotFoundException("Account not found");
              }
            }
          );
        }
      )
      .onItem()
      .transformToUni(
        tuple -> {
          Talent t = tuple.getItem1();
          Account a = tuple.getItem2();
          change.relation(t, a);
          return t.update();
        }
      )
      .onItemOrFailure()
      .transformToUni(
        (t, v) -> {
          return Uni.createFrom().completionStage(msg.ack());
        }
      );
  }
}
