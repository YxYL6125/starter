package com.yxyl.hystrix;

import com.yxyl.hystrix.annotation.DoHystrix;
import com.yxyl.hystrix.value.IValueService;
import com.yxyl.hystrix.value.impl.HystrixValveImpl;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class DoHystrixPoint {

    @Pointcut("@annotation(com.yxyl.hystrix.annotation.DoHystrix)")
    public void aopPoint() {

    }

    @Around("aopPoint()&&@annotation(doGovern)")
    public Object doRouter(ProceedingJoinPoint point, DoHystrix doGovern) throws Throwable {
        IValueService valueService = new HystrixValveImpl();
        return valueService.access(point, getMethod(point), doGovern, point.getArgs());
    }


    private Method getMethod(JoinPoint point) throws NoSuchMethodException {
        Signature signature = point.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        return point.getTarget().getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());

    }
}

