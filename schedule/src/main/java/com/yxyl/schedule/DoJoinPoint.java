package com.yxyl.schedule;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @program: starter
 * @description: 做切面
 * @author: YxYL
 * @create: 2023-06-27 13:44
 **/
@Aspect
@Component("yxyl-middleware-schedule")
public class DoJoinPoint {


    private static final Logger logger = LoggerFactory.getLogger(DoJoinPoint.class);

    @Pointcut("@annotation(com.yxyl.schedule.annotation.DcsScheduled)")
    public void aopPoint() {
    }

    @Around("aopPoint()")
    public Object doRouter(ProceedingJoinPoint jp) throws Throwable {
        long begin = System.currentTimeMillis();
        Method method = getMethod(jp);

        try {
            return jp.proceed();
        } finally {
            long end = System.currentTimeMillis();
//            logger.info("\nyxyl middleware schedule method：{}.{} take time(m)：{}", jp.getTarget().getClass().getSimpleName(), method.getName(), (end - begin));
        }

    }


    public Method getMethod(JoinPoint jp) throws NoSuchMethodException {
        Signature signature = jp.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        return getClass(jp).getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
    }


    public Class<? extends Object> getClass(JoinPoint jp) {
        return jp.getTarget().getClass();
    }

}
