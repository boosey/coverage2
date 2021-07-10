package coverage;

import coverage.framework.AssignRelationFunction;
import coverage.framework.ServiceInterface;
import coverage.framework.ServiceSuper;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

@Path("/accounts")
public class AccountService extends ServiceSuper implements ServiceInterface {

  @Inject
  @Channel("account-event-emitter")
  Emitter<JsonObject> eventEmitter;

  @Inject
  Logger log;

  AccountService() {
    super(
      () -> Account.listAll(),
      id -> Account.findByIdOptional(id),
      () -> Account.deleteAll(),
      id -> Account.deleteById(id)
    );
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<Response> add(Account a, @Context UriInfo uriInfo) {
    return this.addEntity(a, uriInfo);
  }

  @PUT
  @Path("/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<Response> update(String id, Account updates) {
    return this.updateEntity(id, updates);
  }

  @POST
  @Path("/{accountId}/btcManager/{talentId}")
  public Uni<Response> assignBTCManager(
    @PathParam("accountId") String accountId,
    @PathParam("talentId") String talentId
  ) {
    final String assignedEventName = "btcManager-assigned";
    final String unassignedEventName = "btcManager-unassigned";

    AssignRelationFunction assign = (account, talent) -> {
      String prevId = ((Account) account).btcManagerId;
      ((Account) account).btcManagerId = ((Talent) talent).id.toString();
      return prevId;
    };

    return assignTalent(
      accountId,
      talentId,
      assign,
      assignedEventName,
      unassignedEventName
    );
  }

  @POST
  @Path("/{accountId}/designManager/{talentId}")
  public Uni<Response> assignDesignManager(
    @PathParam("accountId") String accountId,
    @PathParam("talentId") String talentId
  ) {
    final String assignedEventName = "designManager-assigned";
    final String unassignedEventName = "designManager-unassigned";

    AssignRelationFunction assign = (account, talent) -> {
      String prevId = ((Account) account).designManagerId;
      ((Account) account).designManagerId = ((Talent) talent).id.toString();
      return prevId;
    };

    return assignTalent(
      accountId,
      talentId,
      assign,
      assignedEventName,
      unassignedEventName
    );
  }

  @POST
  @Path("/{accountId}/squadManager/{talentId}")
  public Uni<Response> assignSquadManager(
    @PathParam("accountId") String accountId,
    @PathParam("talentId") String talentId
  ) {
    final String assignedEventName = "squadManager-assigned";
    final String unassignedEventName = "squadManager-unassigned";

    AssignRelationFunction assign = (account, talent) -> {
      String prevId = ((Account) account).squadManagerId;
      ((Account) account).squadManagerId = ((Talent) talent).id.toString();
      return prevId;
    };

    return assignTalent(
      accountId,
      talentId,
      assign,
      assignedEventName,
      unassignedEventName
    );
  }

  public Uni<Response> assignTalent(
    @PathParam("accountId") String accountId,
    @PathParam("talentId") String talentId,
    AssignRelationFunction assign,
    String assignedEventName,
    String unassignedEventName
  ) {
    return Uni
      .combine()
      .all()
      .unis(
        Account.findByIdOptional(new ObjectId(accountId)),
        Talent.findByIdOptional(new ObjectId(talentId))
      )
      .asTuple()
      .onItem()
      .transform(
        tuple ->
          tuple.mapItem1(
            a -> {
              if (a.isPresent()) {
                return a.get();
              } else {
                throw new NotFoundException("Account not found");
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
          Account a = (Account) tuple.getItem1();
          Talent t = (Talent) tuple.getItem2();
          String prevId = assign.relation(a, t);
          return Uni
            .combine()
            .all()
            .unis(
              a.update(),
              Uni.createFrom().item(Optional.ofNullable(prevId))
            )
            .asTuple();
        }
      )
      .onItem()
      .transform(
        tuple -> {
          Uni<Void> assignedEvent = Uni
            .createFrom()
            .completionStage(
              eventEmitter.send(
                new JsonObject()
                  .put("event", assignedEventName)
                  .put("accountId", accountId)
                  .put("talentId", talentId)
              )
            );

          if (tuple.getItem2().isPresent()) {
            String prevId = tuple.getItem2().get();
            Uni<Void> unassignedEvent = Uni
              .createFrom()
              .completionStage(
                eventEmitter.send(
                  new JsonObject()
                    .put("event", unassignedEventName)
                    .put("accountId", accountId)
                    .put("talentId", prevId)
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
