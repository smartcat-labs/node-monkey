package io.smartcat.node.monkey;

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
import java.util.Map;

public class MonkeyQueryHandler implements QueryHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MonkeyQueryHandler.class);

    private QueryHandler queryHandler = QueryProcessor.instance;

    public MonkeyQueryHandler() {
        LOGGER.info("Started node monkey...");
    }

    public ResultMessage process(String s, QueryState queryState, QueryOptions queryOptions,
            Map<String, ByteBuffer> map) throws RequestExecutionException, RequestValidationException {
        LOGGER.info("Intercepted process");
        return queryHandler.process(s, queryState, queryOptions, map);
    }

    public ResultMessage.Prepared prepare(String s, QueryState queryState, Map<String, ByteBuffer> map)
            throws RequestValidationException {
        LOGGER.info("Intercepted prepare");
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
        return queryHandler.processPrepared(cqlStatement, queryState, queryOptions, map);
    }

    public ResultMessage processBatch(BatchStatement batchStatement, QueryState queryState,
            BatchQueryOptions batchQueryOptions, Map<String, ByteBuffer> map)
            throws RequestExecutionException, RequestValidationException {
        LOGGER.info("Intercepted processBatch");
        return queryHandler.processBatch(batchStatement, queryState, batchQueryOptions, map);
    }
}
