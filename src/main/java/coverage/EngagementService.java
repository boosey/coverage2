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
@Path("/engagements")
public class EngagementService
  extends ServiceSuper
  implements
    BaseServiceMixin<Engagement>, FormRelationMixin<Engagement, Talent> {

  EngagementService(
    @Channel("engagement-event-emitter") Emitter<JsonObject> eventEmitter
  ) {
    super(
      () -> Engagement.listAll(),
      id -> Engagement.findByIdOptional(id),
      () -> Engagement.deleteAll(),
      id -> Engagement.deleteById(id)
    );
    this.emitter = eventEmitter;
  }

  @POST
  @Path("/{engagementId}/engagementManager/{talentId}")
  public Uni<Response> assignEngagementManager(
    @PathParam("engagementId") String engagementId,
    @PathParam("talentId") String talentId
  ) {
    FormRelationFunction<Engagement, Talent> form = (engagement, talent) -> {
      String prevId = engagement.engagementManagerId;
      String newId = talent.id.toString();
      if (newId.equals(prevId)) {
        return null;
      } else {
        engagement.engagementManagerId = newId;
        return prevId;
      }
    };

    return assignTalent(
      Engagement.findByIdOptional(new ObjectId(engagementId)),
      Talent.findByIdOptional(new ObjectId(talentId)),
      form,
      config.event().engagementManagerAssigned(),
      config.event().engagementManagerUnassigned()
    );
  }

  @POST
  @Path("/{engagementId}/engagementLeader/{talentId}")
  public Uni<Response> assignEngagementLeader(
    @PathParam("engagementId") String engagementId,
    @PathParam("talentId") String talentId
  ) {
    FormRelationFunction<Engagement, Talent> form = (engagement, talent) -> {
      String prevId = engagement.engagementLeaderId;
      String newId = talent.id.toString();
      if (newId.equals(prevId)) {
        return null;
      } else {
        engagement.engagementLeaderId = newId;
        return prevId;
      }
    };

    return assignTalent(
      Engagement.findByIdOptional(new ObjectId(engagementId)),
      Talent.findByIdOptional(new ObjectId(talentId)),
      form,
      config.event().engagementLeaderAssigned(),
      config.event().engagementLeaderUnassigned()
    );
  }

  @POST
  @Path("/{engagementId}/talent/{talentId}")
  public Uni<Response> assignTalent(
    @PathParam("engagementId") String engagementId,
    @PathParam("talentId") String talentId
  ) {
    FormRelationFunction<Engagement, Talent> form = (engagement, talent) -> {
      String newId = talent.id.toString();

      engagement.assignTalent(newId);
      return null;
    };

    return assignTalent(
      Engagement.findByIdOptional(new ObjectId(engagementId)),
      Talent.findByIdOptional(new ObjectId(talentId)),
      form,
      config.event().engagementTalentAssigned(),
      config.event().engagementTalentUnassigned()
    );
  }
}
