# Node monkey

Node monkey intercepts Cassandra queries and delays or aborts them.

## How to use it?

Before proceeding, please make sure to install the [Apache Maven][www-maven]. Once installed,
checkout this repository, and execute the following command from the project root:

```
mvn clean install
```

Once the command is executed, copy the resulting JAR file onto the Cassandra classpath. Node monkey
can be configured using an external YAML configuration. The following snippet serves both as a
configuration template, as well as an overview of the default values used if no external
configuration is provided:

```yaml
# percentage of failed requests
failedRequestsPercentage: 10

# percentage of delayed requests
delayedRequestsPercentage: 20

# latency in milliseconds
requestLatency: 30
```

> Note that any invalid property provided through the configuration file,
e.g. negative latency, will cause the configuration to be replaced with
the default one.

Finally, to configure Cassandra to use the node monkey, append the following commands at the end of
`cassandra-env.sh`:

```bash
JVM_OPTS="$JVM_OPTS -Dcassandra.custom_query_handler_class=io.smartcat.node.monkey.LatencyMonkey"

# if external configuration file monkey-config.yml exists
JVM_OPTS="$JVM_OPTS -Dnode.monkey.config=monkey-config.yml"
```

To complete the installation, save the file and restart Cassandra.

[www-maven]: https://maven.apache.org
