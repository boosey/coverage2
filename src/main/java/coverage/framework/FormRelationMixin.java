package coverage.framework;

import coverage.Configuration;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import java.util.Optional;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.eclipse.microprofile.reactive.messaging.Emitter;

public interface FormRelationMixin<P extends EntitySuper, C extends EntitySuper> {
  public Emitter<JsonObject> getEventEmitter();

  public Configuration getConfig();

  public default Uni<Response> assignTalent(
    Uni<Optional<P>> parentUni,
    Uni<Optional<C>> childUni,
    FormRelationFunction form,
    String assignedEventName,
    String unassignedEventName
  ) {
    return Uni
      .combine()
      .all()
      .unis(parentUni, childUni)
      .asTuple()
      .onItem()
      .transform(
        tuple ->
          tuple.mapItem1(
            a -> {
              if (a.isPresent()) {
                return a.get();
              } else {
                throw new NotFoundException("Parent not found");
              }
            }
          )
      )
      .onItem()
      .transform(
        tuple ->
          tuple.mapItem2(
            t -> {
              if (t.isPresent()) {
                return t.get();
              } else {
                throw new NotFoundException("Talent not found");
              }
            }
          )
      )
      .onItem()
      .transformToUni(
        tuple -> {
          P a = tuple.getItem1();
          C t = tuple.getItem2();
          String prevId = form.relation(a, t);
          return Uni
            .combine()
            .all()
            .<EntitySuper, String, Optional<String>>unis(
              a.update(),
              Uni.createFrom().item(t.id.toString()),
              Uni.createFrom().item(Optional.ofNullable(prevId))
            )
            .asTuple();
        }
      )
      .onItem()
      .transform(
        tuple -> {
          EntitySuper p = tuple.getItem1();
          Uni<Void> assignedEvent = Uni
            .createFrom()
            .completionStage(
              getEventEmitter()
                .send(
                  new JsonObject()
                    .put(
                      getConfig().event().property().name(),
                      assignedEventName
                    )
                    .put(getConfig().event().property().parentId(), p.id)
                    .put(
                      getConfig().event().property().talentId(),
                      tuple.getItem2()
                    )
                )
            );

          if (tuple.getItem3().isPresent()) {
            String prevId = tuple.getItem3().get();
            Uni<Void> unassignedEvent = Uni
              .createFrom()
              .completionStage(
                getEventEmitter()
                  .send(
                    new JsonObject()
                      .put(
                        getConfig().event().property().name(),
                        unassignedEventName
                      )
                      .put(getConfig().event().property().parentId(), p.id)
                      .put(getConfig().event().property().talentId(), prevId)
                  )
              );

            return Uni.combine().all().unis(assignedEvent, unassignedEvent);
          } else {
            return assignedEvent;
          }
        }
      )
      .onItem()
      .transform(
        a -> {
          return Response.ok().build();
        }
      )
      .onFailure(error -> error.getClass() == NotFoundException.class)
      .recoverWithItem(
        error -> {
          return Response
            .status(Status.NOT_FOUND)
            .entity(error.getMessage())
            .build();
        }
      )
      .onFailure()
      .recoverWithItem(
        err -> {
          return Response
            .status(Status.INTERNAL_SERVER_ERROR)
            .entity(err)
            .build();
        }
      );
  }
}
