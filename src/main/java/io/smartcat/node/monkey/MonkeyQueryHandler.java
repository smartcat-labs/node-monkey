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

public class MonkeyQueryHandler implements QueryHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonkeyQueryHandler.class);

    private QueryHandler realQueryHandler = QueryProcessor.instance;

    public MonkeyQueryHandler() {
        LOGGER.info("Started node monkey...");
    }

    public ResultMessage process(String query, QueryState queryState, QueryOptions queryOptions)
            throws RequestExecutionException, RequestValidationException {
        LOGGER.info("Intercepted process");
        return realQueryHandler.process(query, queryState, queryOptions);
    }

    public ResultMessage.Prepared prepare(String query, QueryState queryState) throws RequestValidationException {
        LOGGER.info("Intercepted prepare");
        return realQueryHandler.prepare(query, queryState);
    }

    public ParsedStatement.Prepared getPrepared(MD5Digest md5Digest) {
        LOGGER.info("Intercepted getPrepared");
        return realQueryHandler.getPrepared(md5Digest);
    }

    public ParsedStatement.Prepared getPreparedForThrift(Integer integer) {
        LOGGER.info("Intercepted getPreparedForThrift");
        return realQueryHandler.getPreparedForThrift(integer);
    }

    public ResultMessage processPrepared(CQLStatement cqlStatement, QueryState queryState, QueryOptions queryOptions)
            throws RequestExecutionException, RequestValidationException {
        LOGGER.info("Intercepted processPrepared");
        return realQueryHandler.processPrepared(cqlStatement, queryState, queryOptions);
    }

    public ResultMessage processBatch(BatchStatement batchStatement, QueryState queryState,
            BatchQueryOptions batchQueryOptions) throws RequestExecutionException, RequestValidationException {
        LOGGER.info("Intercepted processBatch");
        return realQueryHandler.processBatch(batchStatement, queryState, batchQueryOptions);
    }
}
