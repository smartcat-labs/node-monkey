package io.smartcat.node.monkey.config;

/**
 * This class represents a Node Monkey configuration.
 */
public class Configuration {
    private static final int FAILED_REQUESTS_PERCENTAGE = 10;
    private static final int DELAYED_REQUESTS_PERCENTAGE = 20;
    private static final int REQUEST_LATENCY_IN_MILLISECONDS = 30;

    /**
     * Specifies percentage of failed requests.
     */
    public int failedRequestsPercentage = FAILED_REQUESTS_PERCENTAGE;

    /**
     * Specifies percentage of delayed requests.
     */
    public int delayedRequestsPercentage = DELAYED_REQUESTS_PERCENTAGE;

    /**
     * Specifies the request latency in milliseconds.
     */
    public int requestLatency = REQUEST_LATENCY_IN_MILLISECONDS;

    /**
     * Loads the default configuration when no external configuration is provided.
     *
     * @return Configuration object with default parameters
     */
    public static Configuration loadDefaults() {
        return new Configuration();
    }

    /**
     * Determines whether or not a configuration is valid.
     *
     * @return true if configuration is valid, false otherwise
     */
    boolean isValid() {
        return (failedRequestsPercentage > 0 && delayedRequestsPercentage > 0)
                && (failedRequestsPercentage + delayedRequestsPercentage <= 100)
                && !(delayedRequestsPercentage > 0 && requestLatency < 1);
    }

    @Override
    public String toString() {
        return "{ failedRequestsPercentage: " + failedRequestsPercentage + ", delayedRequestsPercentage: "
                + delayedRequestsPercentage + ", requestLatency: " + requestLatency + '}';
    }
}
