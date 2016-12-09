package io.smartcat.node.monkey.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * YAML based implementation of {@link ConfigurationLoader}.
 */
public final class YamlConfigurationLoader implements ConfigurationLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(YamlConfigurationLoader.class);
    private static final String CONFIGURATION_PROPERTY_NAME = "node.monkey.config";
    private static final String EXTERNAL_CONFIGURATION_NAME = "node-monkey-default.yml";

    /**
     * Determines and returns the external configuration URL.
     *
     * @return {@link URL} configuration file URL
     * @throws ConfigurationException in case of a bogus URL
     */
    private URL configURL() throws ConfigurationException {
        String configUrl = System.getProperty(CONFIGURATION_PROPERTY_NAME);
        if (configUrl == null) {
            configUrl = EXTERNAL_CONFIGURATION_NAME;
            LOGGER.info("Using default configuration {}", EXTERNAL_CONFIGURATION_NAME);
        }

        URL url;
        try {
            url = new URL(configUrl);
            url.openStream().close(); // catches well-formed but bogus URLs
        } catch (Exception err) {
            ClassLoader loader = YamlConfigurationLoader.class.getClassLoader();
            url = loader.getResource(configUrl);
            if (url == null) {
                String required = "file:" + File.separator + File.separator;
                if (!configUrl.startsWith(required)) {
                    throw new ConfigurationException("Expecting URI in variable [" + CONFIGURATION_PROPERTY_NAME + "]. "
                            + "Please prefix the file with " + required + File.separator + " for local files or "
                            + required + "<server>" + File.separator + " for remote files. Aborting.");
                }

                throw new ConfigurationException(
                        "Cannot locate " + configUrl + ".  If this is a local file, please confirm you've provided "
                                + required + File.separator + " as a URI prefix.");
            }
        }

        return url;
    }

    @Override
    public Configuration load() throws ConfigurationException {
        return load(configURL());
    }

    @Override
    public Configuration load(URL url) throws ConfigurationException {
        try {
            LOGGER.info("Loading settings from {}", url);

            Constructor constructor = new Constructor(Configuration.class);
            Yaml yaml = new Yaml(constructor);
            Configuration result;

            try (InputStream is = url.openStream()) {
                result = yaml.loadAs(is, Configuration.class);
            } catch (IOException e) {
                throw new AssertionError(e);
            }

            if (result == null) {
                throw new ConfigurationException("Invalid yaml");
            }
            return result;
        } catch (YAMLException e) {
            throw new ConfigurationException("Invalid yaml", e);
        }
    }
}
