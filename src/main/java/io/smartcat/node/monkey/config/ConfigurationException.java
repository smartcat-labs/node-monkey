package io.smartcat.node.monkey.config;

/**
 * Configuration exception.
 */
public final class ConfigurationException extends Exception {
    /**
     * Default constructor.
     */
    public ConfigurationException() {
        super();
    }

    /**
     * Constructor.
     *
     * @param message error message
     */
    public ConfigurationException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param message error message
     * @param cause   underlying exception
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     *
     * @param cause underlying exception
     */
    public ConfigurationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor.
     *
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public ConfigurationException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
