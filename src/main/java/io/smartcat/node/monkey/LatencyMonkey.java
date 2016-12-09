package io.smartcat.node.monkey;

import io.smartcat.node.monkey.config.Configuration;
import io.smartcat.node.monkey.config.ConfigurationException;
import io.smartcat.node.monkey.config.ConfigurationLoader;
import io.smartcat.node.monkey.config.YamlConfigurationLoader;
import org.apache.cassandra.cql3.BatchQueryOptions;
import org.apache.cassandra.cql3.CQLStatement;
import org.apache.cassandra.cql3.QueryHandler;
import org.apache.cassandra.cql3.QueryOptions;
import org.apache.cassandra.cql3.QueryProcessor;
import org.apache.cassandra.cql3.statements.BatchStatement;
import org.apache.cassandra.cql3.statements.ParsedStatement;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.service.QueryState;
import org.apache.cassandra.transport.messages.ResultMessage;
import org.apache.cassandra.utils.MD5Digest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public final class LatencyMonkey implements QueryHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(LatencyMonkey.class);
    private static Configuration config;

    private enum Action {
        ABORT {
            void apply() {
                throw new RuntimeException();
            }
        },
        DELAY {
            void apply() {
                try {
                    Thread.sleep(config.requestLatency);
                } catch (InterruptedException e) {
                    LOGGER.warn("Interrupted sleep thread.", e);
                }
            }
        },
        EXECUTE;

        void apply() {}
    }

    private Action[] actions = new Action[100];
    private AtomicInteger nextAction = new AtomicInteger();

    private QueryHandler queryHandler = QueryProcessor.instance;

    public LatencyMonkey() {
        loadConfiguration();
        distributeActions();
        shuffleActions();
        LOGGER.info("Started node monkey...");
    }

    private void loadConfiguration() {
        ConfigurationLoader loader = new YamlConfigurationLoader();
        try {
            config = loader.load();
        } catch (ConfigurationException e) {
            LOGGER.error("A problem occurred while loading configuration.", e);
            throw new IllegalStateException(e);
        }
    }

    private void distributeActions() {
        Arrays.fill(actions, 0, config.delayedRequestsPercentage, Action.DELAY);
        Arrays.fill(actions, config.delayedRequestsPercentage, config.failedRequestsPercentage, Action.ABORT);
        Arrays.fill(actions, config.failedRequestsPercentage, actions.length, Action.EXECUTE);
    }

    /*
     * Fisher-Yates array shuffling algorithm.
     */
    private void shuffleActions() {
        Random random = ThreadLocalRandom.current();

        for (int i = 0; i < actions.length; i++) {
            int j = random.nextInt(i + 1);

            Action a = actions[j];
            actions[j] = actions[i];
            actions[i] = a;
        }
    }

    public ResultMessage process(String s, QueryState queryState, QueryOptions queryOptions,
            Map<String, ByteBuffer> map) throws RequestExecutionException, RequestValidationException {
        LOGGER.info("Intercepted process");
        applyNext();
        return queryHandler.process(s, queryState, queryOptions, map);
    }

    public ResultMessage.Prepared prepare(String s, QueryState queryState, Map<String, ByteBuffer> map)
            throws RequestValidationException {
        LOGGER.info("Intercepted prepare");
        applyNext();
        return queryHandler.prepare(s, queryState, map);
    }

    public ParsedStatement.Prepared getPrepared(MD5Digest md5Digest) {
        LOGGER.info("Intercepted getPrepared");
        return queryHandler.getPrepared(md5Digest);
    }

    public ParsedStatement.Prepared getPreparedForThrift(Integer integer) {
        LOGGER.info("Intercepted getPreparedForThrift");
        return queryHandler.getPreparedForThrift(integer);
    }

    public ResultMessage processPrepared(CQLStatement cqlStatement, QueryState queryState, QueryOptions queryOptions,
            Map<String, ByteBuffer> map) throws RequestExecutionException, RequestValidationException {
        LOGGER.info("Intercepted processPrepared");
        applyNext();
        return queryHandler.processPrepared(cqlStatement, queryState, queryOptions, map);
    }

    public ResultMessage processBatch(BatchStatement batchStatement, QueryState queryState,
            BatchQueryOptions batchQueryOptions, Map<String, ByteBuffer> map)
            throws RequestExecutionException, RequestValidationException {
        LOGGER.info("Intercepted processBatch");
        applyNext();
        return queryHandler.processBatch(batchStatement, queryState, batchQueryOptions, map);
    }

    private void applyNext() {
        int next = nextAction.getAndIncrement() % actions.length;
        if (next == actions.length - 1) {
            nextAction.set(0);
        }

        actions[next].apply();
    }
}
