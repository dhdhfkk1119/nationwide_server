package com.nationwide.nationwide_server.core.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    // Controller 전역 로깅 모든 Controller 메서드가 실행되기 “직전”에
    @Before("execution(* com.nationwide.nationwide_server..*Controller.*(..))")
    public void logBeforeController(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        log.info("Controller 호출: {} / 파라미터: {}", methodName, Arrays.toString(args));
    }

    // Service 전역 로깅
    @Before("execution(* com.nationwide.nationwide_server..*Service.*(..))")
    public void logBeforeService(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        log.info("Service 실행: {} / 파라미터: {}", methodName, Arrays.toString(args));
    }

    // 메서드 종료 후 로깅
    @AfterReturning(pointcut = "execution(* com.nationwide.nationwide_server..*(..))", returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().toShortString();
        log.info("메서드 종료: {} / 반환값: {}", methodName, result);
    }
}
