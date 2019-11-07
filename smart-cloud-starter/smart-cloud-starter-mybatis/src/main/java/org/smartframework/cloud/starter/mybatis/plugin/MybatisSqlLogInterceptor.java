package org.smartframework.cloud.starter.mybatis.plugin;

import java.util.Properties;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.smartframework.cloud.mask.util.MaskUtil;
import org.smartframework.cloud.starter.log.util.LogUtil;

import lombok.extern.slf4j.Slf4j;


/**
 * mybatis sql日志打印
 *
 * @author liyulin
 * @date 2019-03-22
 */
@Intercepts({ @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }),
		@Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class }),
		@Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class }),
		@Signature(type = Executor.class, method = "queryCursor", args = { MappedStatement.class, Object.class, RowBounds.class }) })
@Slf4j
public class MybatisSqlLogInterceptor implements Interceptor {

	/**sql最大长度限制*/
	private static final int SQL_MAX_LEN = 1<<8;

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		Object returnValue = null;
		long start = System.currentTimeMillis();
		try {
			returnValue = invocation.proceed();
			return returnValue;
		} finally {
			long end = System.currentTimeMillis();
			long time = (end - start);
			MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
			BoundSql boundSql = null;
			if (invocation.getArgs().length == 6) {
				boundSql = (BoundSql) invocation.getArgs()[5];
			} else {
				Object parameter = invocation.getArgs()[1];
				boundSql = mappedStatement.getBoundSql(parameter);
			}
			printSql(boundSql, mappedStatement.getId(), time, returnValue);
		}
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {

	}
	
	/**
	 * 打印sql日志
	 * 
	 * @param boundSql
	 * @param sqlId
	 * @param time
	 * @param returnValue
	 */
	public static void printSql(BoundSql boundSql, String sqlId, long time, Object returnValue) {
		String separator = " ==> ";
		String sql = boundSql.getSql();
		StringBuilder str = new StringBuilder((sql.length() >= SQL_MAX_LEN) ? SQL_MAX_LEN : 64);
		str.append(sqlId);
		str.append("：");
		str.append(boundSql.getSql());
		str.append(separator);
		str.append(MaskUtil.mask(boundSql.getParameterObject()));
		str.append(separator);
		str.append("spend：");
		str.append(time);
		str.append("ms");
		str.append(separator);
		str.append("result==> ");
		str.append(MaskUtil.mask(returnValue));

		log.info(LogUtil.truncate(str.toString()));
	}

//	/**
//	 * sql日志拼接
//	 * 
//	 * <p>
//	 * 不能用换行。如果使用换行，在logstash中日志的顺序将会混乱
//	 * 
//	 * @param configuration
//	 * @param boundSql
//	 * @param sqlId
//	 * @param time
//	 * @param returnValue
//	 */
//	public static void showSql(Configuration configuration, BoundSql boundSql, String sqlId, long time,
//			Object returnValue) {
//		String separator = " ==> ";
//		String sql = getSql(configuration, boundSql);
//		StringBuilder str = new StringBuilder((sql.length() > 256) ? 256 : 64);
//		str.append(sqlId);
//		str.append("：");
//		str.append(sql);
//		str.append(separator);
//		str.append("spend：");
//		str.append(time);
//		str.append("ms");
//		str.append(separator);
//		str.append("result===>");
//		str.append(MaskUtil.mask(returnValue));
//
//		log.info(LogUtil.truncate(str.toString()));
//	}
//
//	private static String getParameterValue(Object obj) {
//		String params = "";
//		if (obj instanceof String) {
//			params = "'" + obj + "'";
//		} else if (obj instanceof Date) {
//			Date date = (Date) obj;
//			params = "'" + DateUtil.formatDateTime(date) + "'";
//		} else if (Objects.isNull(obj)) {
//			params = "null";
//		} else {
//			params = obj.toString();
//		}
//
//		return Matcher.quoteReplacement(params);
//	}
//
//	private static final String QUOTE = "\\?";
//	public static String getSql(Configuration configuration, BoundSql boundSql) {
//		Object parameterObject = boundSql.getParameterObject();
//		List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
//
//		String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
//		if (CollectionUtils.isEmpty(parameterMappings) || Objects.isNull(parameterObject)) {
//			return sql;
//		}
//
//		TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
//		if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
//			sql = sql.replaceFirst(QUOTE, getParameterValue(parameterObject));
//		} else {
//			MetaObject metaObject = configuration.newMetaObject(parameterObject);
//			for (ParameterMapping parameterMapping : parameterMappings) {
//				String propertyName = parameterMapping.getProperty();
//				if (metaObject.hasGetter(propertyName)) {
//					Object obj = metaObject.getValue(propertyName);
//					sql = sql.replaceFirst(QUOTE, getParameterValue(obj));
//				} else if (boundSql.hasAdditionalParameter(propertyName)) {
//					Object obj = boundSql.getAdditionalParameter(propertyName);
//					sql = sql.replaceFirst(QUOTE, getParameterValue(obj));
//				}
//			}
//		}
//
//		return sql;
//	}

}