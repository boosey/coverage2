package coverage.framework;

import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.bson.types.ObjectId;

public interface ServiceSuperInterface {
  public Uni<Response> list();

  public <T extends EntitySuper> Uni<List<T>> listUni();

  public Uni<Response> delete();

  public Uni<Long> deleteAllUni();

  public Uni<Response> deleteById(@PathParam("id") String id);

  public Uni<Boolean> deleteByIdUni(ObjectId id);

  public <T extends EntitySuper> Uni<Optional<T>> findByIdOptionalUni(
    ObjectId id
  );

  public Uni<Response> findById(@PathParam("id") String id);

  public <E extends EntitySuper> Uni<Response> addEntity(
    E account,
    UriInfo uriInfo
  );

  public <E extends EntitySuper> Uni<Response> updateEntity(
    String id,
    E updates
  );
}
