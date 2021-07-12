package coverage.framework;

import coverage.Configuration;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public interface ServiceMixin<E extends EntitySuper> {
  public ListAllUniFunction getListAllUniFunction();

  public DeleteAllUniFunction getDeleteAllUniFunction();

  public DeleteByIdUniFunction getDeleteByIdUniFunction();

  public FindByIdOptionalUniFunction getFindByIdOptionalUniFunction();

  public Emitter<JsonObject> getEventEmitter();

  public Configuration getConfig();

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public default Uni<Response> list() {
    return getListAllUniFunction()
      .apply()
      .onItem()
      .transform(items -> Response.ok().entity(items).build())
      .onFailure()
      .recoverWithItem(
        error ->
          Response.status(Status.INTERNAL_SERVER_ERROR).entity(error).build()
      );
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}")
  public default Uni<Response> findById(@PathParam("id") String id) {
    return getFindByIdOptionalUniFunction()
      .apply(new ObjectId(id))
      .onItem()
      .transform(
        item -> {
          if (item.isPresent()) {
            return Response.ok().entity(item.get()).build();
          } else {
            return Response.status(Status.NOT_FOUND).build();
          }
        }
      )
      .onFailure()
      .recoverWithItem(
        err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build()
      );
  }

  @POST
  public default Uni<Response> add(E entity, @Context UriInfo uriInfo) {
    return entity
      .persist()
      .onItem()
      .transformToUni(
        e ->
          Uni
            .createFrom()
            .completionStage(
              getEventEmitter()
                .send(
                  new JsonObject()
                  .put(
                      getConfig().event().property().name(),
                      getConfig().event().entityCreated()
                    )
                )
            )
      )
      .onItem()
      .transform(
        v ->
          Response
            .status(Status.CREATED)
            .entity(
              uriInfo
                .getAbsolutePathBuilder()
                .segment(entity.id.toString())
                .build()
                .toString()
            )
            .build()
      )
      .onFailure()
      .recoverWithItem(
        err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build()
      );
  }

  @PUT
  @Path("/{id}")
  public default Uni<Response> update(String id, E updates) {
    return getFindByIdOptionalUniFunction()
      .apply(new ObjectId(id))
      .onItem()
      .transform(
        itemOpt -> {
          if (itemOpt.isPresent()) {
            return itemOpt.get();
          } else {
            throw new NotFoundException();
          }
        }
      )
      .onItem()
      .transformToUni(
        item -> {
          item.updateFields(updates);
          return item.update();
        }
      )
      .onItem()
      .transformToUni(
        e ->
          Uni
            .createFrom()
            .completionStage(
              getEventEmitter()
                .send(
                  new JsonObject()
                  .put(
                      getConfig().event().property().name(),
                      getConfig().event().entityUpdated()
                    )
                )
            )
      )
      .onItem()
      .transform(v -> Response.ok().build())
      .onFailure(error -> error.getClass() == NotFoundException.class)
      .recoverWithItem(Response.status(Status.NOT_FOUND).build())
      .onFailure()
      .recoverWithItem(
        err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build()
      );
  }

  @DELETE
  public default Uni<Response> delete() {
    return getDeleteAllUniFunction()
      .apply()
      .onItem()
      .transformToUni(
        e ->
          Uni
            .createFrom()
            .completionStage(
              getEventEmitter()
                .send(
                  new JsonObject()
                  .put(
                      getConfig().event().property().name(),
                      getConfig().event().entityDeletedAll()
                    )
                )
            )
      )
      .onItem()
      .transform(count -> Response.ok().entity(count).build())
      .onFailure()
      .recoverWithItem(Response.status(Status.INTERNAL_SERVER_ERROR).build());
  }

  @DELETE
  @Path("/{id}")
  public default Uni<Response> deleteById(@PathParam("id") String id) {
    return getDeleteByIdUniFunction()
      .apply(new ObjectId(id))
      .onItem()
      .transformToUni(
        succeeded ->
          Uni
            .createFrom()
            .completionStage(
              getEventEmitter()
                .send(
                  new JsonObject()
                  .put(
                      getConfig().event().property().name(),
                      getConfig().event().entityDeleted()
                    )
                )
            )
            .onItem()
            .transform(v -> succeeded)
      )
      .onItem()
      .transform(
        succeeded -> {
          if (succeeded) {
            return Response.ok().build();
          } else {
            return Response.status(Status.NOT_FOUND).build();
          }
        }
      )
      .onFailure()
      .recoverWithItem(Response.status(Status.INTERNAL_SERVER_ERROR).build());
  }
}
