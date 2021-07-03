package coverage;

import coverage.framework.AssignRelationFunction;
import coverage.framework.EventProcessingSuper;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import io.vertx.core.json.JsonObject;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.reactive.messaging.Incoming;

public class TalentEventProcessing extends EventProcessingSuper {

  @Incoming("account-event")
  public Uni<Void> consumeAccountEvent(
    KafkaRecord<JsonObject, JsonObject> msg
  ) {
    AssignRelationFunction assign = (parent, relationId) -> {
      Talent t = (Talent) parent;
      t.assignAccount(relationId);
    };

    JsonObject p = msg.getPayload();
    return this.processNewRelationship(
        msg,
        Talent.<Talent>findByIdOptional(new ObjectId(p.getString("talentId"))),
        Account.<Account>findByIdOptional(
          new ObjectId(p.getString("accountId"))
        ),
        assign
      );
  }
}
