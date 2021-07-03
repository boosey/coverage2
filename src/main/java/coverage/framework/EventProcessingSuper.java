package coverage.framework;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import io.vertx.core.json.JsonObject;
import java.lang.Void;
import java.util.Optional;
import javax.ws.rs.NotFoundException;

public class EventProcessingSuper {

  public <P extends EntitySuper, C extends EntitySuper> Uni<Void> processNewRelationship(
    KafkaRecord<JsonObject, JsonObject> msg,
    Uni<Optional<P>> parent,
    Uni<Optional<C>> child,
    AssignRelationFunction assign
  ) {
    JsonObject payload = msg.getPayload();
    switch (payload.getString("event")) {
      case "btcManager-assigned":
        return Uni
          .combine()
          .all()
          .unis(parent, child)
          .asTuple()
          .onItem()
          .transformToUni(
            tuple -> {
              if (
                tuple.getItem1().isPresent() && tuple.getItem2().isPresent()
              ) {
                P p = tuple.getItem1().get();
                C c = tuple.getItem2().get();
                assign.relation(p, c.id.toString());
                return p.update();
              } else {
                throw new NotFoundException();
              }
            }
          )
          .onItem()
          .transformToUni(
            item -> {
              return Uni.createFrom().completionStage(msg.ack());
            }
          )
          .onFailure()
          .recoverWithItem(
            err -> {
              // needs to go in dead letter queue
              msg.ack();
              return null;
            }
          );
      default:
        // need to put in dead letter queue - unknown event
        return Uni.createFrom().completionStage(msg.ack());
    }
  }
}
