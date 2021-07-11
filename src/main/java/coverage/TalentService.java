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

@Path("/talent")
public class TalentService
  extends ServiceSuper
  implements ServiceInterface, AssignTalent<Talent, Talent> {

  @Inject
  @Channel("talent-event-emitter")
  Emitter<JsonObject> eventEmitter;

  @Inject
  Configuration config;

  TalentService() {
    super(
      () -> Talent.listAll(),
      id -> Talent.findByIdOptional(id),
      () -> Talent.deleteAll(),
      id -> Talent.deleteById(id)
    );
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<Response> add(Talent a, @Context UriInfo uriInfo) {
    return this.addEntity(a, uriInfo);
  }

  @PUT
  @Path("/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<Response> update(String id, Talent updates) {
    return this.updateEntity(id, updates);
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

    return assignTalentMixin(
      Talent.findByIdOptional(new ObjectId(talentId)),
      Talent.findByIdOptional(new ObjectId(managerId)),
      assign,
      config.event().talentManagerAssigned(),
      config.event().talentManagerUnassigned(),
      eventEmitter,
      config
    );
  }
}
