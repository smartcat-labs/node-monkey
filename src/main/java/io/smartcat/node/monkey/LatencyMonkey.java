package io.smartcat.node.monkey;

import io.smartcat.node.monkey.config.Configuration;
import io.smartcat.node.monkey.config.ConfigurationException;
import io.smartcat.node.monkey.config.ConfigurationLoader;
import io.smartcat.node.monkey.config.YamlConfigurationLoader;
import org.apache.cassandra.cql3.*;
import org.apache.cassandra.cql3.statements.BatchStatement;
import org.apache.cassandra.cql3.statements.ParsedStatement;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.service.QueryState;
import org.apache.cassandra.transport.messages.ResultMessage;
import org.apache.cassandra.utils.MD5Digest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * LatencyMonkey adds latency to the queries, or fails them completely. The
 * amount of affected queries is set through external configuration.
 */
public final class LatencyMonkey implements QueryHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(LatencyMonkey.class);
    private static final int TOTAL_ACTIONS = 100;
    private static Configuration config;

    private final QueryHandler queryHandler = QueryProcessor.instance;
    private final Action[] actions;
    private final AtomicInteger nextAction;

    /**
     * Default constructor.
     */
    public LatencyMonkey() {
        actions = new Action[TOTAL_ACTIONS];
        nextAction = new AtomicInteger();

        loadConfiguration();
        distributeActions();
        shuffleActions();

        LOGGER.info("Started node monkey with configuration {}", config);
    }

    private void loadConfiguration() {
        ConfigurationLoader loader = new YamlConfigurationLoader();

        try {
            config = loader.load();
        } catch (ConfigurationException e) {
            LOGGER.warn("A problem occurred while loading configuration.", e);
            config = Configuration.loadDefaults();
        }
    }

    private void distributeActions() {
        int index = 0;

        while (index < config.delayedRequestsPercentage) {
            actions[index++] = Action.DELAY;
        }

        int chaosPercentage = config.delayedRequestsPercentage + config.failedRequestsPercentage;
        while (index < chaosPercentage) {
            actions[index++] = Action.ABORT;
        }

        while (index < actions.length) {
            actions[index++] = Action.EXECUTE;
        }
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

    @Override
    public ResultMessage process(String s, QueryState queryState, QueryOptions queryOptions,
            Map<String, ByteBuffer> map) throws RequestExecutionException, RequestValidationException {
        LOGGER.trace("Intercepted process");
        applyNext();
        return queryHandler.process(s, queryState, queryOptions, map);
    }

    @Override
    public ResultMessage.Prepared prepare(String s, QueryState queryState, Map<String, ByteBuffer> map)
            throws RequestValidationException {
        LOGGER.trace("Intercepted prepare");
        applyNext();
        return queryHandler.prepare(s, queryState, map);
    }

    @Override
    public ParsedStatement.Prepared getPrepared(MD5Digest md5Digest) {
        LOGGER.trace("Intercepted getPrepared");
        return queryHandler.getPrepared(md5Digest);
    }

    @Override
    public ParsedStatement.Prepared getPreparedForThrift(Integer integer) {
        LOGGER.trace("Intercepted getPreparedForThrift");
        return queryHandler.getPreparedForThrift(integer);
    }

    @Override
    public ResultMessage processPrepared(CQLStatement cqlStatement, QueryState queryState,
            QueryOptions queryOptions, Map<String, ByteBuffer> map)
            throws RequestExecutionException, RequestValidationException {
        LOGGER.trace("Intercepted processPrepared");
        applyNext();
        return queryHandler.processPrepared(cqlStatement, queryState, queryOptions, map);
    }

    @Override
    public ResultMessage processBatch(BatchStatement batchStatement, QueryState queryState,
            BatchQueryOptions batchQueryOptions, Map<String, ByteBuffer> map)
            throws RequestExecutionException, RequestValidationException {
        LOGGER.trace("Intercepted processBatch");
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

    /**
     * Implements GoF strategy pattern for "chaos" behaviour.
     */
    private enum Action {
        ABORT {
            void apply() {
                throw new InvalidRequestException("Aborted by node monkey.");
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
        EXECUTE {
            void apply() {

            }
        };

        abstract void apply();
    }
}
