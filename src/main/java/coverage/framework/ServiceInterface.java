package coverage.framework;

import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.Response;
import org.bson.types.ObjectId;

public interface ServiceInterface {
  public Uni<Response> list();

  public Uni<Response> findById(String id);

  public Uni<Response> delete();

  public Uni<Response> deleteById(String id);

  public <E extends EntitySuper> Uni<List<E>> listUni();

  public <E extends EntitySuper> Uni<Optional<E>> findByIdOptionalUni(
    ObjectId id
  );

  public Uni<Long> deleteAllUni();

  public Uni<Boolean> deleteByIdUni(ObjectId id);
}
