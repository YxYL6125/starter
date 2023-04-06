package com.yxyl.hystrix.value.impl;

import com.alibaba.fastjson.JSON;
import com.netflix.hystrix.*;
import com.yxyl.hystrix.annotation.DoHystrix;
import com.yxyl.hystrix.value.IValueService;
import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.Method;

public class HystrixValveImpl extends HystrixCommand<Object> implements IValueService {

    private ProceedingJoinPoint point;
    private Method method;
    private DoHystrix doHystrix;

    /*
     * 设置HystrixCommand的属性
     * GroupKey:                该命令属于哪个组，可以帮助我们更好的组织命令
     * CommandKey:              该命令的名称
     * ThreadPoolKey:           该命令所属线程池的名称，同样配置的命令会共享同一个线程池，若不配置，就会默认使用GroupKey作为线程池名称
     * CommandProperties:       该命令的一下设置，包括断路器的配置，隔离策略，降级社会i,以及一些监控指标等
     * ThreadPoolProperties:    关于线程池的配置，包括线程池大小，排队队列的大小等
     */
    public HystrixValveImpl() {
        super(HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("GovernGroup"))
                .andCommandKey(HystrixCommandKey.Factory.asKey("GovernKey"))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("GovernThreadPool"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD))
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withCoreSize(10))
        );
    }

    @Override
    public Object access(ProceedingJoinPoint point, Method method, DoHystrix doHystrix, Object[] args) throws Throwable {

        this.point = point;
        this.method = method;
        this.doHystrix = doHystrix;
        //设置熔断时间
        Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("GovernGroup"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionTimeoutInMilliseconds(doHystrix.timeoutValue()));

        return this.execute();
    }

    @Override
    protected Object run() throws Exception {
        try {
            return point.proceed();
        } catch (Throwable e) {
            return null;
        }

    }

    @Override
    protected Object getFallback() {
        System.out.printf("熔断了");
        return JSON.parseObject(doHystrix.returnJson(), method.getReturnType());
    }
}
