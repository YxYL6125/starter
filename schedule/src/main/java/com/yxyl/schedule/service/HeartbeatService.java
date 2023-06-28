package com.yxyl.schedule.service;

import com.alibaba.fastjson.JSON;
import com.yxyl.schedule.common.ScheduleConstants;
import com.yxyl.schedule.domain.ExecOrder;
import com.yxyl.schedule.task.ScheduledTask;
import com.yxyl.schedule.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.yxyl.schedule.common.ScheduleConstants.Global.*;

/**
 * @program: starter
 * @description: 心跳监测器
 * @author: YxYL
 * @create: 2023-06-28 09:28
 **/

public class HeartbeatService {
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatService.class);

    private ScheduledExecutorService ses;

    /**
     * 单例模式
     */
    private static class SingletonHolder {
        private static final HeartbeatService INSTANCE = new HeartbeatService();
    }

    private HeartbeatService() {

    }

    public static HeartbeatService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void startFlushScheduleStatus() {
        //普通周期任务线程池
//        ses = Executors.newScheduledThreadPool(1);
        
        //虚拟线程周期线程池
        ses = Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().factory());
        
        //300s后，每60s心跳一次
        ses.scheduleAtFixedRate(() -> {
            try {
                System.out.println("Thread.currentThread() = " + Thread.currentThread());
                //执行心跳的内容
                logger.info("middleware schedule heart beat On-Site Inspection task");
                Map<String, ScheduledTask> scheduledTasks = ScheduleConstants.schedulerTasks;
                Map<String, List<ExecOrder>> execOrderMap = ScheduleConstants.execOrderMap;
                Set<String> beanNameSet = execOrderMap.keySet();
                for (String beanName : beanNameSet) {
                    List<ExecOrder> execOrderList = execOrderMap.get(beanName);
                    for (ExecOrder execOrder : execOrderList) {
                        String taskId = execOrder.getBeanName() + "_" + execOrder.getMethodName();
                        ScheduledTask scheduledTask = scheduledTasks.get(taskId);
                        if (scheduledTask == null) {
                            continue;
                        }
                        boolean cancelled = scheduledTask.isCancelled();
                        //路劲拼装
                        String path_root_server_ip_clazz = StrUtil.joinStr(path_root_server_ip, LINE, "clazz", LINE, execOrder.getBeanName());
                        String path_root_server_ip_clazz_method = StrUtil.joinStr(path_root_server_ip_clazz, LINE, "method", LINE, execOrder.getMethodName(), LINE, "value");
                        //获取现有值
                        ExecOrder oldExecOrder;
                        byte[] bytes = client.getData().forPath(path_root_server_ip_clazz_method);
                        if (bytes != null) {
                            String oldJson = new String(bytes, CHARSET_NAME);
                            oldExecOrder = JSON.parseObject(oldJson, ExecOrder.class);
                        } else {
                            oldExecOrder = new ExecOrder();
                            oldExecOrder.setBeanName(execOrder.getBeanName());
                            oldExecOrder.setMethodName(execOrder.getMethodName());
                            oldExecOrder.setDesc(execOrder.getDesc());
                            oldExecOrder.setCron(execOrder.getCron());
                            oldExecOrder.setAutoStartup(execOrder.getAutoStartup());
                        }
                        oldExecOrder.setAutoStartup(!cancelled);

                        //临时节点【数据】
                        if (client.checkExists().forPath(path_root_server_ip_clazz_method) == null) {
                            continue;
                        }
                        String newJson = JSON.toJSONString(oldExecOrder);
                        client.setData().forPath(path_root_server_ip_clazz_method, newJson.getBytes(CHARSET_NAME));

                        //永久节点【数据】
                        String path_root_ip_server_clazz_method_status = StrUtil.joinStr(path_root_server_ip_clazz, LINE, "method", LINE, execOrder.getMethodName(), "/status");
                        if (client.checkExists().forPath(path_root_ip_server_clazz_method_status) == null) {
                            continue;
                        }
                        client.setData().forPath(path_root_ip_server_clazz_method_status, (execOrder.getAutoStartup() ? "1" : "0").getBytes(CHARSET_NAME));
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, 300, 60, TimeUnit.SECONDS);

    }

}
