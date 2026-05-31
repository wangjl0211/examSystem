package cn.org.wang.exam.config;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SQL执行拦截器，用于统计SQL执行信息
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class SqlExecutionInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(SqlExecutionInterceptor.class);

    // SQL执行统计信息
    public static class SqlStatistics {
        // 操作类型统计
        public final ConcurrentMap<String, AtomicInteger> operationCount = new ConcurrentHashMap<>();
        // 总执行次数
        public final AtomicInteger totalCount = new AtomicInteger(0);
        // 总执行时间(ms)
        public final AtomicLong totalTime = new AtomicLong(0);
        // 耗时分布统计
        public final ConcurrentMap<String, AtomicInteger> durationDistribution = new ConcurrentHashMap<>();

        public SqlStatistics() {
            // 初始化操作类型统计
            operationCount.put("Select", new AtomicInteger(0));
            operationCount.put("Update", new AtomicInteger(0));
            operationCount.put("Delete", new AtomicInteger(0));
            operationCount.put("Insert", new AtomicInteger(0));
            // 初始化耗时分布统计
            durationDistribution.put("<100ms", new AtomicInteger(0));
            durationDistribution.put("100-500ms", new AtomicInteger(0));
            durationDistribution.put(">500ms", new AtomicInteger(0));
        }

        // 重置统计信息
        public void reset() {
            operationCount.forEach((k, v) -> v.set(0));
            totalCount.set(0);
            totalTime.set(0);
            durationDistribution.forEach((k, v) -> v.set(0));
        }
    }

    // 全局统计信息
    private static final SqlStatistics statistics = new SqlStatistics();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        String sqlCommandType = mappedStatement.getSqlCommandType().name();

        // 开始时间
        long startTime = System.currentTimeMillis();

        try {
            // 执行SQL
            return invocation.proceed();
        } finally {
            // 结束时间
            long endTime = System.currentTimeMillis();
            // 执行耗时
            long duration = endTime - startTime;

            // 统计SQL执行信息
            statistics.totalCount.incrementAndGet();
            statistics.totalTime.addAndGet(duration);

            // 统计操作类型
            String operationType = getOperationType(sqlCommandType);
            statistics.operationCount.getOrDefault(operationType, new AtomicInteger(0)).incrementAndGet();

            // 统计耗时分布
            String durationRange = getDurationRange(duration);
            statistics.durationDistribution.getOrDefault(durationRange, new AtomicInteger(0)).incrementAndGet();

            // 日志记录（可选，仅记录耗时较长的SQL）
            if (duration > 500) {
                BoundSql boundSql = mappedStatement.getBoundSql(invocation.getArgs()[1]);
                logger.warn("检测到慢SQL: {}ms - {}", duration, boundSql.getSql());
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可以通过properties配置拦截器参数
    }

    // 获取操作类型
    private String getOperationType(String sqlCommandType) {
        switch (sqlCommandType) {
            case "SELECT":
                return "Select";
            case "UPDATE":
                return "Update";
            case "DELETE":
                return "Delete";
            case "INSERT":
                return "Insert";
            default:
                return "Other";
        }
    }

    // 获取耗时范围
    private String getDurationRange(long duration) {
        if (duration < 100) {
            return "<100ms";
        } else if (duration < 500) {
            return "100-500ms";
        } else {
            return ">500ms";
        }
    }

    // 获取统计信息
    public static SqlStatistics getStatistics() {
        return statistics;
    }
}
