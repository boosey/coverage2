package coverage.framework;

import coverage.Configuration;
import io.vertx.core.json.JsonObject;
import javax.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

public class ServiceSuper {

  @Inject
  protected Configuration config;

  @Inject
  protected Logger log;

  private ListAllUniFunction listAllUniFunction;
  private FindByIdOptionalUniFunction findByIdOptionalUniFunction;
  private DeleteAllUniFunction deleteAllUniFunction;
  private DeleteByIdUniFunction deleteByIdUniFunction;

  private Emitter<JsonObject> emitter;

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

  protected void eventEmitter(Emitter<JsonObject> subclassEmitter) {
    this.emitter = subclassEmitter;
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

  public Emitter<JsonObject> getEventEmitter() {
    return emitter;
  }
}
