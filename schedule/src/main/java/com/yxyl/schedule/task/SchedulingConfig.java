package com.yxyl.schedule.task;

import com.yxyl.schedule.common.ScheduleConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ThreadFactory;

/**
 * @program: starter
 * @description: 配置类
 * @author: YxYL
 * @create: 2023-06-27 13:59
 **/
@Configuration("yxyl-middleware-schedule-schedulingConfig")
public class SchedulingConfig {

    @Bean(value = "yxyl-middleware-schedule-taskSchedule")
    public TaskScheduler taskScheduler() {
        //传统任务
//        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();

        
        //虚拟线程工厂对应的taskScheduler
        ThreadFactory virtualThreadFactory = Thread.ofVirtual().factory();
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setThreadFactory(virtualThreadFactory);
        taskScheduler.initialize();
        taskScheduler.setPoolSize(ScheduleConstants.Global.schedulePoolSize);
        taskScheduler.setRemoveOnCancelPolicy(true);
        taskScheduler.setThreadNamePrefix("YxYLMiddlewareScheduleThreadPool");
        return taskScheduler;
    }


}
