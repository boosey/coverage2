package coverage;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "config")
public interface Configuration {
  Event event();

  public interface Event {
    String btcManagerAssigned();
    String btcManagerUnassigned();
    String designManagerAssigned();
    String designManagerUnassigned();
    String squadManagerAssigned();
    String squadManagerUnassigned();
  }
}
