package coverage;

import coverage.framework.AssignRelationFunction;
import coverage.framework.AssignTalentMixin;
import coverage.framework.ServiceMixin;
import coverage.framework.ServiceSuper;
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
@Path("/talent")
public class TalentService
  extends ServiceSuper
  implements ServiceMixin<Talent>, AssignTalentMixin<Talent, Talent> {

  TalentService(
    @Channel("talent-event-emitter") Emitter<JsonObject> eventEmitter
  ) {
    super(
      () -> Talent.listAll(),
      id -> Talent.findByIdOptional(id),
      () -> Talent.deleteAll(),
      id -> Talent.deleteById(id)
    );
    this.emitter = eventEmitter;
  }

  @POST
  @Path("/{talentId}/manager/{managerId}")
  public Uni<Response> assignTalentManager(
    @PathParam("talentId") String talentId,
    @PathParam("managerId") String managerId
  ) {
    AssignRelationFunction assign = (talent, manager) -> {
      String prevId = ((Talent) talent).managerId;
      String newId = ((Talent) manager).id.toString();
      if (newId.equals(prevId)) {
        return null;
      } else {
        ((Talent) talent).managerId = newId;
        return prevId;
      }
    };

    return assignTalent(
      Talent.findByIdOptional(new ObjectId(talentId)),
      Talent.findByIdOptional(new ObjectId(managerId)),
      assign,
      config.event().talentManagerAssigned(),
      config.event().talentManagerUnassigned()
    );
  }
}
