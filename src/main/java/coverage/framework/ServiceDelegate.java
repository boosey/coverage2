package coverage.framework;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.bson.types.ObjectId;

@ApplicationScoped
public class ServiceDelegate {

  public <S extends ServiceSuper> Uni<Response> list(S svc) {
    return svc
      .getListAllUniFunction()
      .apply()
      .onItem()
      .transform(items -> Response.ok().entity(items).build())
      .onFailure()
      .recoverWithItem(
        error ->
          Response.status(Status.INTERNAL_SERVER_ERROR).entity(error).build()
      );
  }

  public <S extends ServiceSuper> Uni<Response> findById(S svc, String id) {
    return svc
      .getFindByIdOptionalUniFunction()
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

  public <S extends ServiceSuperInterface> Uni<Response> add(
    S svc,
    ReactivePanacheMongoEntity entity,
    UriInfo uriInfo
  ) {
    return entity
      .persist()
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

  public <S extends ServiceSuper> Uni<Response> delete(S svc) {
    return svc
      .getDeleteAllUniFunction()
      .apply()
      .onItem()
      .transform(count -> Response.ok().entity(count).build())
      .onFailure()
      .recoverWithItem(Response.status(Status.INTERNAL_SERVER_ERROR).build());
  }

  public <S extends ServiceSuper> Uni<Response> deleteById(S svc, String id) {
    return svc
      .getDeleteByIdUniFunction()
      .apply(new ObjectId(id))
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

  public <E extends EntitySuper, S extends ServiceSuper> Uni<Response> update(
    S svc,
    String id,
    E updates
  ) {
    return svc
      .getFindByIdOptionalUniFunction()
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
      .transform(v -> Response.ok().build())
      .onFailure(error -> error.getClass() == NotFoundException.class)
      .recoverWithItem(Response.status(Status.NOT_FOUND).build())
      .onFailure()
      .recoverWithItem(
        err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build()
      );
  }

  public <P extends EntitySuper, C extends EntitySuper> Uni<Response> assignRelation(
    Uni<Optional<P>> parent,
    Uni<Optional<C>> child,
    AssignRelationFunction assign
  ) {
    return Uni
      .combine()
      .all()
      .unis(parent, child)
      .asTuple()
      .onItem()
      .transform(
        tuple -> {
          if (tuple.getItem1().isPresent() && tuple.getItem2().isPresent()) {
            return Tuple2.of(
              tuple.getItem1().get(),
              tuple.getItem2().get().id.toString()
            );
          } else {
            throw new NotFoundException();
          }
        }
      )
      .onItem()
      .transformToUni(
        tuple -> {
          EntitySuper p = tuple.getItem1();
          String relatedId = tuple.getItem2();
          assign.relation(p, relatedId);
          return p.update();
        }
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
}
