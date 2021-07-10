package coverage;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import io.vertx.core.json.JsonObject;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

public class TalentEventProcessing {

  @Inject
  Logger log;

  @Incoming("account-event")
  public Uni<Void> consumeAccountEvent(
    KafkaRecord<JsonObject, JsonObject> msg
  ) {
    log.info("processing account-event: " + msg.getTopic());
    JsonObject p = msg.getPayload();
    switch (p.getString("event")) {
      case "btcManager-assigned":
        log.info("processing btcManager-assigned");
        return processAccountTalentRelationshipFormed(msg);
      case "btcManager-unassigned":
      // return processAccountTalentRelationship(msg, unassign);
    }
    return Uni.createFrom().completionStage(msg.ack());
  }

  public Uni<Void> processAccountTalentRelationshipFormed(
    KafkaRecord<JsonObject, JsonObject> msg
  ) {
    JsonObject p = msg.getPayload();

    return Uni
      .combine()
      .all()
      .unis(
        Talent.<Talent>findByIdOptional(new ObjectId(p.getString("talentId"))),
        Account.<Account>findByIdOptional(
          new ObjectId(p.getString("accountId"))
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
          t.assignAccount(a.id.toString());
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
