package com.yxyl.schedule.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * @program: starter
 * @description:
 * @author: YxYL
 * @create: 2023-06-27 13:56
 **/

public class SchedulingRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SchedulingRunnable.class);

    private Object bean;
    private String beanName;
    private String methodName;

    public SchedulingRunnable(Object bean, String beanName, String methodName) {
        this.bean = bean;
        this.beanName = beanName;
        this.methodName = methodName;
    }


    @Override
    public void run() {
        try {
            logger.info("当前线程:{}", Thread.currentThread());
            Method method = bean.getClass().getDeclaredMethod(methodName);
            ReflectionUtils.makeAccessible(method);
            method.invoke(bean);
        } catch (Exception e) {
            logger.error("middleware schedule error:", e);
        }

    }


    public String taskId() {
        return beanName + "_" + methodName;
    }
}
