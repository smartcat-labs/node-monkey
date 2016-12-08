package io.smartcat.node.monkey.config;

import java.net.URL;

/**
 * Configuration loader.
 */
public interface ConfigurationLoader {
    /**
     * Loads configuration from an implicit location.
     *
     * @return loaded configuration
     * @throws ConfigurationException in case the configuration cannot be loaded
     */
    Configuration load() throws ConfigurationException;

    /**
     * Loads configuration using an explicit location.
     *
     * @param url configuration location
     * @return loaded configuration
     * @throws ConfigurationException in case the configuration cannot be loaded
     */
    Configuration load(URL url) throws ConfigurationException;
}
