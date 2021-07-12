package coverage;

import coverage.framework.BaseServiceMixin;
import coverage.framework.FormRelationMixin;
import coverage.framework.ServiceSuper;
import coverage.framework.functionalinterfaces.FormRelationFunction;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import javax.inject.Singleton;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@Singleton
@Path("/accounts")
public class AccountService
  extends ServiceSuper
  implements BaseServiceMixin<Account>, FormRelationMixin<Account, Talent> {

  AccountService(
    @Channel("account-event-emitter") Emitter<JsonObject> eventEmitter
  ) {
    super(
      () -> Account.listAll(),
      id -> Account.findByIdOptional(id),
      () -> Account.deleteAll(),
      id -> Account.deleteById(id)
    );
    this.emitter = eventEmitter;
  }

  @POST
  @Path("/{accountId}/btcManager/{talentId}")
  public Uni<Response> assignBTCManager(
    @PathParam("accountId") String accountId,
    @PathParam("talentId") String talentId
  ) {
    FormRelationFunction assign = (account, talent) -> {
      String prevId = ((Account) account).btcManagerId;
      String newId = ((Talent) talent).id.toString();
      if (newId.equals(prevId)) {
        return null;
      } else {
        ((Account) account).btcManagerId = newId;
        return prevId;
      }
    };

    return assignTalent(
      Account.findByIdOptional(new ObjectId(accountId)),
      Talent.findByIdOptional(new ObjectId(talentId)),
      assign,
      config.event().accountBtcManagerAssigned(),
      config.event().accountBtcManagerUnassigned()
    );
  }

  @POST
  @Path("/{accountId}/designManager/{talentId}")
  public Uni<Response> assignDesignManager(
    @PathParam("accountId") String accountId,
    @PathParam("talentId") String talentId
  ) {
    FormRelationFunction assign = (account, talent) -> {
      String prevId = ((Account) account).designManagerId;
      String newId = ((Talent) talent).id.toString();
      if (newId.equals(prevId)) {
        return null;
      } else {
        ((Account) account).designManagerId = newId;
        return prevId;
      }
    };

    return assignTalent(
      Account.findByIdOptional(new ObjectId(accountId)),
      Talent.findByIdOptional(new ObjectId(talentId)),
      assign,
      config.event().accountDesignManagerAssigned(),
      config.event().accountDesignManagerUnassigned()
    );
  }

  @POST
  @Path("/{accountId}/squadManager/{talentId}")
  public Uni<Response> assignSquadManager(
    @PathParam("accountId") String accountId,
    @PathParam("talentId") String talentId
  ) {
    FormRelationFunction assign = (account, talent) -> {
      String prevId = ((Account) account).squadManagerId;
      String newId = ((Talent) talent).id.toString();
      if (newId.equals(prevId)) {
        return null;
      } else {
        ((Account) account).squadManagerId = newId;
        return prevId;
      }
    };

    return assignTalent(
      Account.findByIdOptional(new ObjectId(accountId)),
      Talent.findByIdOptional(new ObjectId(talentId)),
      assign,
      config.event().accountSquadManagerAssigned(),
      config.event().accountSquadManagerUnassigned()
    );
  }
}
