package io.hydev.currency.exchange.configuration;

import io.micrometer.tracing.ScopedSpan;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Slf4j
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "${shedlock.global.lock-at-most-for}")
class SchedulerConfiguration {

    @Bean
    LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(dataSource);
    }

    @Aspect
    @Component
    @RequiredArgsConstructor
    public static class InitializeScheduledJobsTracingAspect {

        private final Tracer tracer;

        @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
        public Object initializeScheduledJobTracing(ProceedingJoinPoint joinPoint) throws Throwable {
            ScopedSpan span = tracer.startScopedSpan(
                    "Execution of %s.%s".formatted(joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName()));
            try {
                return joinPoint.proceed();
            } finally {
                span.end();
            }
        }
    }
}
