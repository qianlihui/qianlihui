package com.qlh.base.mybatis;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.lang.reflect.Method;

@Slf4j
@Data
public class SnailDataSourceRouter extends AbstractRoutingDataSource {

    private ThreadLocal<String> determineCurrentLookupKey = ThreadLocal.withInitial(() -> "");

    @Override
    protected Object determineCurrentLookupKey() {
        return determineCurrentLookupKey.get();
    }

    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        String lastDS = determineCurrentLookupKey.get();
        String targetDS = getSwitchDSName(joinPoint);
        if (StringUtils.isNotBlank(targetDS)) {
//            log.info("切换到数据源 {}", targetDS);
            determineCurrentLookupKey.set(targetDS);
        }
        try {
            return joinPoint.proceed(joinPoint.getArgs());
        } finally {
            determineCurrentLookupKey.set(lastDS);
        }
    }

    private static String getSwitchDSName(JoinPoint joinPoint) {
        try {
            Class clazz = joinPoint.getTarget().getClass();
            Method method = clazz.getMethod(joinPoint.getSignature().getName(),
                    ((MethodSignature) joinPoint.getSignature()).getMethod().getParameterTypes());
            SnailDataSourceSelector selector = method.getAnnotation(SnailDataSourceSelector.class);
            if (selector == null) {
                selector = (SnailDataSourceSelector) clazz.getAnnotation(SnailDataSourceSelector.class);
            }
            return selector == null ? "" : selector.name();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
