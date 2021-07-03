package coverage.framework;

import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.bson.types.ObjectId;

public class ServiceSuper implements ServiceSuperInterface {

  @Inject
  ServiceDelegate delegate;

  private ListAllUniFunction listAllUniFunction;
  private FindByIdOptionalUniFunction findByIdOptionalUniFunction;
  private DeleteAllUniFunction deleteAllUniFunction;
  private DeleteByIdUniFunction deleteByIdUniFunction;

  public ServiceSuper(
    ListAllUniFunction listAllUniFunction,
    FindByIdOptionalUniFunction findByIdOptionalUniFunction,
    DeleteAllUniFunction deleteAllUniFunction,
    DeleteByIdUniFunction deleteByIdUniFunction
  ) {
    this.listAllUniFunction = listAllUniFunction;
    this.findByIdOptionalUniFunction = findByIdOptionalUniFunction;
    this.deleteAllUniFunction = deleteAllUniFunction;
    this.deleteByIdUniFunction = deleteByIdUniFunction;
  }

  public ListAllUniFunction getListAllUniFunction() {
    return listAllUniFunction;
  }

  public FindByIdOptionalUniFunction getFindByIdOptionalUniFunction() {
    return findByIdOptionalUniFunction;
  }

  public DeleteAllUniFunction getDeleteAllUniFunction() {
    return deleteAllUniFunction;
  }

  public DeleteByIdUniFunction getDeleteByIdUniFunction() {
    return deleteByIdUniFunction;
  }

  Uni<List<EntitySuper>> listAllUni() {
    return listAllUniFunction.apply();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Response> list() {
    return delegate.list(this);
  }

  @DELETE
  public Uni<Response> delete() {
    return delegate.delete(this);
  }

  @DELETE
  @Path("/{id}")
  public Uni<Response> deleteById(@PathParam("id") String id) {
    return delegate.deleteById(this, id);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}")
  public Uni<Response> findById(@PathParam("id") String id) {
    return delegate.findById(this, id);
  }

  public <E extends EntitySuper> Uni<Response> addEntity(
    E account,
    UriInfo uriInfo
  ) {
    return delegate.add(this, account, uriInfo);
  }

  public <E extends EntitySuper> Uni<Response> updateEntity(
    String id,
    E updates
  ) {
    return delegate.update(this, id, updates);
  }

  @Override
  public <T extends EntitySuper> Uni<List<T>> listUni() {
    throw new UnsupportedOperationException(
      "A ServiceSuper sublcass must implement this method"
    );
  }

  public Uni<Long> deleteAllUni() {
    throw new UnsupportedOperationException(
      "A ServiceSuper sublcass must implement this method"
    );
  }

  public Uni<Boolean> deleteByIdUni(ObjectId id) {
    throw new UnsupportedOperationException(
      "A ServiceSuper sublcass must implement this method"
    );
  }

  public <T extends EntitySuper> Uni<Optional<T>> findByIdOptionalUni(
    ObjectId id
  ) {
    throw new UnsupportedOperationException(
      "A ServiceSuper sublcass must implement this method"
    );
  }

  public <P extends EntitySuper, C extends EntitySuper> Uni<Response> assignRelation(
    Uni<Optional<P>> parent,
    Uni<Optional<C>> child,
    AssignRelationFunction assign
  ) {
    return delegate.assignRelation(parent, child, assign);
  }
}
