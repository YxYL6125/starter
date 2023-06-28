package com.yxyl.schedule.common;

import org.apache.curator.framework.CuratorFramework;
import com.yxyl.schedule.domain.ExecOrder;
import com.yxyl.schedule.task.ScheduledTask;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @program: starter
 * @description: 常量类
 * @author: YxYL
 * @create: 2023-06-27 10:45
 **/

public class ScheduleConstants {

    public static final Map<String, List<ExecOrder>> execOrderMap = new ConcurrentHashMap<>();


    /**
     * 默认初始化16
     */
    public static final Map<String, ScheduledTask> schedulerTasks = new ConcurrentHashMap<>(16);

    public static class Global {
        public static ApplicationContext applicationContext;
        public static final String LINE = "/";
        public static String CHARSET_NAME = "utf-8";
        public static int schedulePoolSize = 8;                      //定时任务执行线程核心线程数
        public static String ip;                                     //本机IP
        public static String zkAddress;                              //zk服务地址：{host}:{port}
        public static String schedulerServerId;                      //任务服务ID：工程名称EN
        public static String schedulerServerName;                    //任务服务名称：工程名称CN
        public static CuratorFramework client;                       //zk配置
        public static String path_root = "/com/yxyl/schedule";
        public static String path_root_exec = path_root + "/exec";
        public static String path_root_server;
        public static String path_root_server_ip;
        public static String path_root_server_ip_clazz;              //[结构标记]类名称
        public static String path_root_server_ip_clazz_method;       //[结构标记]临时节点
        public static String path_root_server_ip_clazz_method_status;//[结构标记]永久标记


    }


    public static class InstructStatus {
        public static final Integer stop = 0;       //停止
        public static final Integer start = 1;      //启动
        public static final Integer refresh = 2;    //刷新
    }


}
