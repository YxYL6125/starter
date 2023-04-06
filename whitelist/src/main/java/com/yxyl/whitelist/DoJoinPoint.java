package com.yxyl.whitelist;

import com.alibaba.fastjson.JSON;
import com.yxyl.whitelist.annotation.WhiteList;
import org.apache.commons.beanutils.BeanUtils;
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

import javax.annotation.Resource;
import java.lang.reflect.Method;

@Aspect
@Component
public class DoJoinPoint {


    private static final Logger logger = LoggerFactory.getLogger(DoJoinPoint.class);
    @Resource
    private String whiteListConfig;

    @Pointcut("@annotation(com.yxyl.whitelist.annotation.WhiteList)")
    public void aopPoint() {
    }


    @Around("aopPoint()")
    public Object doRouter(ProceedingJoinPoint point) throws Throwable {
        //获取内容
        Method method = getMethod(point);
        WhiteList whiteList = method.getAnnotation(WhiteList.class);
        //获取字段值
        String keyValue = getFiledValue(whiteList.key(), point.getArgs());
        logger.info("middleware whitelist handler method：{} value：{}", method.getName(), keyValue);
        if (null == keyValue || "".equals(keyValue)) {
            return point.proceed();
        }

        String[] split = whiteListConfig.split(",");
        //白名单过滤

        for (String str : split) {
            if (keyValue.equals(str)) {
                return point.proceed();
            }
        }

        //拦截
        return returnObject(whiteList, method);
    }

    private Object returnObject(WhiteList whiteList, Method method) throws InstantiationException, IllegalAccessException {
        Class<?> returnType = method.getReturnType();
        String returnJson = whiteList.returnJson();
        if ("".equals(returnJson)) {
            return returnType.newInstance();
        }
        return JSON.parseObject(returnJson, returnType);
    }

    // 获取属性值
    private String getFiledValue(String filed, Object[] args) {
        String filedValue = null;
        for (Object arg : args) {
            try {
                if (null == filedValue || "".equals(filedValue)) {
                    filedValue = BeanUtils.getProperty(arg, filed);
                } else {
                    break;
                }
            } catch (Exception e) {
                if (args.length == 1) {
                    return args[0].toString();
                }
            }
        }
        return filedValue;
    }

    private Method getMethod(JoinPoint point) throws NoSuchMethodException {
        Signature signature = point.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        return point.getTarget().getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
    }
}
