package com.yxyl.hystrix.value;

import com.yxyl.hystrix.annotation.DoHystrix;
import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.Method;

public interface IValueService {
    Object access(ProceedingJoinPoint point, Method method, DoHystrix doHystrix, Object[] args) throws Throwable;
}
