package coverage;

import coverage.framework.AssignRelationFunction;
import coverage.framework.AssignTalent;
import coverage.framework.ServiceMixin;
import coverage.framework.ServiceSuper;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@Path("/engagements")
public class EngagementService
  extends ServiceSuper
  implements ServiceMixin<Engagement>, AssignTalent<Engagement, Talent> {

  @Inject
  @Channel("engagement-event-emitter")
  Emitter<JsonObject> eventEmitter;

  EngagementService() {
    super(
      () -> Engagement.listAll(),
      id -> Engagement.findByIdOptional(id),
      () -> Engagement.deleteAll(),
      id -> Engagement.deleteById(id)
    );
    eventEmitter(eventEmitter);
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
