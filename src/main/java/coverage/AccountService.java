package coverage;

import coverage.framework.AssignRelationFunction;
import coverage.framework.AssignTalent;
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
import javax.ws.rs.core.UriInfo;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

@Path("/accounts")
public class AccountService
  extends ServiceSuper
  implements ServiceInterface, AssignTalent<Account, Talent> {

  @Inject
  @Channel("account-event-emitter")
  Emitter<JsonObject> eventEmitter;

  @Inject
  Configuration config;

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
    AssignRelationFunction assign = (account, talent) -> {
      String prevId = ((Account) account).btcManagerId;
      String newId = ((Talent) talent).id.toString();
      if (newId.equals(prevId)) {
        return null;
      } else {
        ((Account) account).btcManagerId = newId;
        return prevId;
      }
    };

    return assignTalentMixin(
      Account.findByIdOptional(new ObjectId(accountId)),
      Talent.findByIdOptional(new ObjectId(talentId)),
      assign,
      config.event().btcManagerAssigned(),
      config.event().btcManagerUnassigned(),
      eventEmitter,
      config
    );
  }

  @POST
  @Path("/{accountId}/designManager/{talentId}")
  public Uni<Response> assignDesignManager(
    @PathParam("accountId") String accountId,
    @PathParam("talentId") String talentId
  ) {
    AssignRelationFunction assign = (account, talent) -> {
      String prevId = ((Account) account).designManagerId;
      String newId = ((Talent) talent).id.toString();
      if (newId.equals(prevId)) {
        return null;
      } else {
        ((Account) account).designManagerId = newId;
        return prevId;
      }
    };

    return assignTalentMixin(
      Account.findByIdOptional(new ObjectId(accountId)),
      Talent.findByIdOptional(new ObjectId(talentId)),
      assign,
      config.event().designManagerAssigned(),
      config.event().designManagerUnassigned(),
      eventEmitter,
      config
    );
  }

  @POST
  @Path("/{accountId}/squadManager/{talentId}")
  public Uni<Response> assignSquadManager(
    @PathParam("accountId") String accountId,
    @PathParam("talentId") String talentId
  ) {
    AssignRelationFunction assign = (account, talent) -> {
      String prevId = ((Account) account).squadManagerId;
      String newId = ((Talent) talent).id.toString();
      if (newId.equals(prevId)) {
        return null;
      } else {
        ((Account) account).squadManagerId = newId;
        return prevId;
      }
    };

    return assignTalentMixin(
      Account.findByIdOptional(new ObjectId(accountId)),
      Talent.findByIdOptional(new ObjectId(talentId)),
      assign,
      config.event().squadManagerAssigned(),
      config.event().squadManagerUnassigned(),
      eventEmitter,
      config
    );
  }
}
