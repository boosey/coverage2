package coverage;

import coverage.framework.AssignRelationFunction;
import coverage.framework.ServiceInterface;
import coverage.framework.ServiceSuper;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
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

@Path("/accounts")
public class AccountService extends ServiceSuper implements ServiceInterface {

  @Inject
  @Channel("account-event-emitter")
  Emitter<JsonObject> eventEmitter;

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
  @Path("/{accountId}/squadManager/{talentId}")
  public Uni<Response> assignSquadManager(
    @PathParam("accountId") String accountId,
    @PathParam("talentId") String talentId
  ) {
    AssignRelationFunction assign = (parent, childId) -> {
      Account account = (Account) parent;
      account.squadManagerId = childId;
    };

    return this.assignRelation(
        Account.findByIdOptional(new ObjectId(accountId)),
        Talent.findByIdOptional(new ObjectId(talentId)),
        assign
      );
  }

  @POST
  @Path("/{accountId}/designManager/{talentId}")
  public Uni<Response> assignDesignManager(
    @PathParam("accountId") String accountId,
    @PathParam("talentId") String talentId
  ) {
    AssignRelationFunction assign = (parent, childId) -> {
      Account account = (Account) parent;
      account.designManagerId = childId;
    };

    return this.assignRelation(
        Account.findByIdOptional(new ObjectId(accountId)),
        Talent.findByIdOptional(new ObjectId(talentId)),
        assign
      );
  }

  @POST
  @Path("/{accountId}/btcManager/{talentId}")
  public Uni<Response> assignBTCManager(
    @PathParam("accountId") String accountId,
    @PathParam("talentId") String talentId
  ) {
    AssignRelationFunction assign = (parent, childId) -> {
      Account account = (Account) parent;
      account.btcManagerId = childId;
    };

    return this.assignRelation(
        Account.findByIdOptional(new ObjectId(accountId)),
        Talent.findByIdOptional(new ObjectId(talentId)),
        assign
      )
      .onItem()
      .invoke(
        response -> {
          if (response.getStatus() == Status.OK.getStatusCode()) {
            JsonObject e = new JsonObject()
              .put("event", "btcManager-assigned")
              .put("accountId", accountId)
              .put("talentId", talentId);

            eventEmitter.send(e);
          }
        }
      );
  }
}
