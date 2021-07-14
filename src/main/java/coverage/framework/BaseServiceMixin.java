package coverage.framework;

import coverage.framework.functionalinterfaces.DeleteAllUniFunction;
import coverage.framework.functionalinterfaces.DeleteByIdUniFunction;
import coverage.framework.functionalinterfaces.FindByIdOptionalUniFunction;
import coverage.framework.functionalinterfaces.ListAllUniFunction;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
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
public interface BaseServiceMixin<E extends EntitySuper> {
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
      .<EntitySuper>persist()
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
                    .put(getConfig().event().property().id(), e.id.toString())
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
                    .put(getConfig().event().property().id(), id)
                )
            )
      )
      .onItem()
      .transform(v -> Response.ok().build())
      .onFailure(
        error -> {
          return error.getClass() == NotFoundException.class;
        }
      )
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
    // return getListAllUniFunction()
    //   .apply()
    //   .onItem()
    //   .transform(
    //     list -> {
    //       return list
    //         .stream()
    //         .map(e -> e.id.toString())
    //         .collect(Collectors.toSet());
    //     }
    //   )
    //   .onItem()
    //   .transformToUni(
    //     idSet -> {
    //       return Uni
    //         .combine()
    //         .all()
    //         .unis(
    //           Uni.createFrom().item(idSet),
    //           getDeleteAllUniFunction().apply()
    //         )
    //         .asTuple();
    //     }
    //   )
    //   .onItem()
    //   .transformToUni(
    //     tuple -> {
    //       if (tuple.getItem1().size() == tuple.getItem2()) {
    //         return Uni
    //           .combine()
    //           .all()
    //           .unis(
    //             tuple
    //               .getItem1()
    //               .stream()
    //               .map(
    //                 id -> {
    //                   return Uni
    //                     .createFrom()
    //                     .completionStage(
    //                       getEventEmitter()
    //                         .send(
    //                           new JsonObject()
    //                             .put(
    //                               getConfig().event().property().name(),
    //                               getConfig().event().entityDeleted()
    //                             )
    //                             .put(getConfig().event().property().id(), id)
    //                         )
    //                     );
    //                 }
    //               )
    //               .collect(Collectors.toSet())
    //           )
    //           .combinedWith(
    //             emittedEventConfirmationList ->
    //               emittedEventConfirmationList.size()
    //           )
    //           .onItem()
    //           .transform(
    //             count -> {
    //               return Response.ok().entity(count).build();
    //             }
    //           )
    //           .onFailure()
    //           .recoverWithItem(
    //             Response.status(Status.INTERNAL_SERVER_ERROR).build()
    //           );
    //       } else {
    //         throw new InternalServerErrorException(
    //           "Number of items doesn't match the deleted records"
    //         );
    //       }
    //     }
    //   );
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
                    .put(getConfig().event().property().id(), id)
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
