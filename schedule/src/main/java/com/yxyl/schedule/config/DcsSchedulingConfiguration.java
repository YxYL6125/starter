package com.yxyl.schedule.config;

import com.alibaba.fastjson.JSON;
import com.yxyl.schedule.annotation.DcsScheduled;
import com.yxyl.schedule.common.ScheduleConstants;
import com.yxyl.schedule.domain.ExecOrder;
import com.yxyl.schedule.service.HeartbeatService;
import com.yxyl.schedule.service.ZkCuratorServer;
import com.yxyl.schedule.task.CronTaskRegister;
import com.yxyl.schedule.task.SchedulingRunnable;
import com.yxyl.schedule.util.StrUtil;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.yxyl.schedule.common.ScheduleConstants.Global.*;

/**
 * @program: starter
 * @description:
 * @author: YxYL
 * @create: 2023-06-27 20:59
 **/
@Configuration("yxyl-middleware-schedule-dcsSchedulingConfiguration")
public class DcsSchedulingConfiguration implements ApplicationContextAware, BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {
    

    private static final Logger logger = LoggerFactory.getLogger(DcsSchedulingConfiguration.class);

    private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ScheduleConstants.Global.applicationContext = applicationContext;
    }


    /**
     * 初始化成功后，执行
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        if (this.nonAnnotatedClasses.contains(targetClass)) {
            return bean;
        }
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(bean.getClass());
        for (Method method : methods) {
            DcsScheduled dcsScheduled = AnnotationUtils.findAnnotation(method, DcsScheduled.class);
            if (dcsScheduled == null || 0 == method.getDeclaredAnnotations().length) {
                continue;
            }
            List<ExecOrder> execOrderList = ScheduleConstants.execOrderMap.computeIfAbsent(beanName, k -> new ArrayList<>());
            
            //set properties
            ExecOrder execOrder = new ExecOrder();
            execOrder.setBean(bean);
            execOrder.setBeanName(beanName);
            execOrder.setMethodName(method.getName());
            execOrder.setDesc(dcsScheduled.desc());
            execOrder.setCron(dcsScheduled.cron());
            execOrder.setAutoStartup(dcsScheduled.autoStartup());
            execOrderList.add(execOrder);
            this.nonAnnotatedClasses.add(targetClass);
        }
        return bean;
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        try {
            ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
            //初始化配置
            initConfig(applicationContext);
            //初始化服务
            initServer(applicationContext);
            //启动任务
            initTask(applicationContext);
            //挂载节点
            initNode(applicationContext);
            //心跳监听
            HeartbeatService.getInstance().startFlushScheduleStatus();
            logger.info("middleware schedule init config、server、task、node、heart done!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 初始化配置
     *
     * @param applicationContext
     */
    private void initConfig(ApplicationContext applicationContext) {
        try {
            StarterServiceProperties properties = applicationContext.getBean("yxyl-middleware-schedule-starterAutoConfig", StarterAutoConfig.class).getProperties();
            zkAddress = properties.getZkAddress();
            ScheduleConstants.Global.schedulerServerId = properties.getSchedulerServerId();
            ScheduleConstants.Global.schedulerServerName = properties.getSchedulerServerName();
            InetAddress id = InetAddress.getLocalHost();
            ScheduleConstants.Global.ip = id.getHostAddress();
        } catch (Exception e) {
            logger.error("middleware schedule init config error！", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 初始化服务
     *
     * @param applicationContext
     */
    private void initServer(ApplicationContext applicationContext) {
        try {
            //获取zk连接
            CuratorFramework clint = ZkCuratorServer.getClint(zkAddress);

            //节点组装
            String path_root_server = StrUtil.joinStr(path_root, LINE, "server", LINE, schedulerServerId);
            String path_root_server_ip = StrUtil.joinStr(path_root_server, "ip", LINE, ip);

            //创建节点&递归删除本服务IP下的旧内容
            ZkCuratorServer.deletingChildrenIfNeeded(clint, path_root_server_ip);
            ZkCuratorServer.createNode(clint, path_root_server_ip);
            ZkCuratorServer.setData(clint, path_root_server, schedulerServerName);

            //添加节点&监听
            ZkCuratorServer.createNodeSimple(clint, path_root_exec);
            ZkCuratorServer.addTreeCacheListener(applicationContext, clint, path_root_exec);
        } catch (Exception e) {
            logger.error("middleware schedule init server error！", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 启动任务
     *
     * @param applicationContext
     */
    private void initTask(ApplicationContext applicationContext) {
        CronTaskRegister cronTaskRegister = applicationContext.getBean("yxyl-middleware-schedule-cronTaskRegister", CronTaskRegister.class);
        Set<String> beanNames = ScheduleConstants.execOrderMap.keySet();
        for (String beanName : beanNames) {
            List<ExecOrder> execOrderList = ScheduleConstants.execOrderMap.get(beanName);
            for (ExecOrder execOrder : execOrderList) {
                SchedulingRunnable task = new SchedulingRunnable(execOrder.getBean(), execOrder.getBeanName(), execOrder.getMethodName());
                cronTaskRegister.addCronTask(task, execOrder.getCron());
            }
        }
    }

    /**
     * 挂载节点
     *
     * @param applicationContext
     */
    private void initNode(ApplicationContext applicationContext) throws Exception {
        Set<String> beanNames = ScheduleConstants.execOrderMap.keySet();
        for (String beanName : beanNames) {
            List<ExecOrder> execOrders = ScheduleConstants.execOrderMap.get(beanName);
            for (ExecOrder execOrder : execOrders) {
                String path_root_server_ip_clazz = StrUtil.joinStr(path_root_server_ip, LINE, "clazz", LINE, execOrder.getBeanName());
                String path_root_server_ip_clazz_method = StrUtil.joinStr(path_root_server_ip_clazz, LINE, "method", LINE, execOrder.getMethodName());
                String path_root_server_ip_clazz_method_status = StrUtil.joinStr(path_root_server_ip_clazz, LINE, "method", LINE, execOrder.getMethodName() + "/status");

                //添加节点
                ZkCuratorServer.createNodeSimple(client, path_root_server_ip_clazz);
                ZkCuratorServer.createNodeSimple(client, path_root_server_ip_clazz_method);
                ZkCuratorServer.createNodeSimple(client, path_root_server_ip_clazz_method_status);

                //添加节点数据【临时】
                ZkCuratorServer.appendPersistentData(client, path_root_server_ip_clazz_method + "/value", JSON.toJSONString(execOrder));
                //添加节点数据【永久】
                ZkCuratorServer.setData(client, path_root_server_ip_clazz_method_status, execOrder.getAutoStartup() ? "1" : "0");
            }
        }
    }
}
