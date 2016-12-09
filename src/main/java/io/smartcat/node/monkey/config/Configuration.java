package io.smartcat.node.monkey.config;

/**
 * This class represents a Node Monkey configuration.
 */
public class Configuration {
    /**
     * Loads the default configuration when no external configuration is provided.
     *
     * @return Configuration object with default parameters
     */
    public static Configuration loadDefaults() {
        return new Configuration();
    }

    /**
     * Specifies percentage of failed requests.
     */
    public Integer failedRequestsPercentage = 10;

    /**
     * Specifies percentage of delayed requests.
     */
    public Integer delayedRequestsPercentage = 20;

    /**
     * Specifies the request latency in milliseconds.
     */
    public Integer requestLatency = 30;
}
