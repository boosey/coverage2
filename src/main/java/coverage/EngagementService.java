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

@Path("/engagements")
public class EngagementService
  extends ServiceSuper
  implements ServiceInterface, AssignTalent<Engagement, Talent> {

  @Inject
  @Channel("engagement-event-emitter")
  Emitter<JsonObject> eventEmitter;

  @Inject
  Configuration config;

  @Inject
  Logger log;

  EngagementService() {
    super(
      () -> Engagement.listAll(),
      id -> Engagement.findByIdOptional(id),
      () -> Engagement.deleteAll(),
      id -> Engagement.deleteById(id)
    );
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<Response> add(Engagement a, @Context UriInfo uriInfo) {
    return this.addEntity(a, uriInfo);
  }

  @PUT
  @Path("/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<Response> update(String id, Engagement updates) {
    return this.updateEntity(id, updates);
  }

  @POST
  @Path("/{engagementId}/engagementManager/{talentId}")
  public Uni<Response> assignEngagementManager(
    @PathParam("engagementId") String engagementId,
    @PathParam("talentId") String talentId
  ) {
    AssignRelationFunction assign = (engagement, talent) -> {
      String prevId = ((Engagement) engagement).engagementManagerId;
      String newId = ((Talent) talent).id.toString();
      if (newId.equals(prevId)) {
        return null;
      } else {
        ((Engagement) engagement).engagementManagerId = newId;
        return prevId;
      }
    };

    return assignTalentMixin(
      Engagement.findByIdOptional(new ObjectId(engagementId)),
      Talent.findByIdOptional(new ObjectId(talentId)),
      assign,
      config.event().engagementManagerAssigned(),
      config.event().engagementManagerUnassigned(),
      eventEmitter,
      config
    );
  }

  @POST
  @Path("/{engagementId}/engagementLeader/{talentId}")
  public Uni<Response> assignEngagementLeader(
    @PathParam("engagementId") String engagementId,
    @PathParam("talentId") String talentId
  ) {
    AssignRelationFunction assign = (engagement, talent) -> {
      String prevId = ((Engagement) engagement).engagementLeaderId;
      String newId = ((Talent) talent).id.toString();
      if (newId.equals(prevId)) {
        return null;
      } else {
        ((Engagement) engagement).engagementLeaderId = newId;
        return prevId;
      }
    };

    return assignTalentMixin(
      Engagement.findByIdOptional(new ObjectId(engagementId)),
      Talent.findByIdOptional(new ObjectId(talentId)),
      assign,
      config.event().engagementLeaderAssigned(),
      config.event().engagementLeaderUnassigned(),
      eventEmitter,
      config
    );
  }

  @POST
  @Path("/{engagementId}/talent/{talentId}")
  public Uni<Response> assignTalent(
    @PathParam("engagementId") String engagementId,
    @PathParam("talentId") String talentId
  ) {
    AssignRelationFunction assign = (engagement, talent) -> {
      String newId = ((Talent) talent).id.toString();

      ((Engagement) engagement).assignTalent(newId);
      return null;
    };

    return assignTalentMixin(
      Engagement.findByIdOptional(new ObjectId(engagementId)),
      Talent.findByIdOptional(new ObjectId(talentId)),
      assign,
      config.event().engagementTalentAssigned(),
      config.event().engagementTalentUnassigned(),
      eventEmitter,
      config
    );
  }
}
