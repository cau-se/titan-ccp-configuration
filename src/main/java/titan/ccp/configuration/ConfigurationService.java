package titan.ccp.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import titan.ccp.configuration.api.ConfigurationRepository;
import titan.ccp.configuration.api.ConfigurationRepository.ConfigurationRepositoryException;
import titan.ccp.configuration.api.RestApiServer;

/**
 * A microservice that manages the system-wide configuration. For example, the sensor registry. It
 * provides a REST interface to get or modify the configuration and optionally publishes changes to
 * a Kafka topic.
 *
 */
public final class ConfigurationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationService.class);

  private RestApiServer webServer;

  private ConfigurationRepository configurationRepository;

  /**
   * Run the microservice.
   */
  public void run() {
    try {
      this.configurationRepository = new ConfigurationRepository();
    } catch (final ConfigurationRepositoryException e) {
      LOGGER.error("Could not instantiate underlying repository.", e);
      this.stop();
    }

    this.webServer = new RestApiServer(
        Config.WEBSERVER_PORT,
        Config.CORS,
        this.configurationRepository);
    this.webServer.start();
  }

  /**
   * Stop the microservice.
   */
  private void stop() {
    LOGGER.warn("Shutting down Configuration microservice.");
    if (this.webServer != null) {
      this.webServer.stop();
    }
    if (this.configurationRepository != null) {
      this.configurationRepository.close();
    }
    System.exit(1); // NOPMD exit application manually
  }

  public static void main(final String[] args) {
    new ConfigurationService().run();
  }
}
