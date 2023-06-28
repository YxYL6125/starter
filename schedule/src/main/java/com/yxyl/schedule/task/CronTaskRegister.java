package com.yxyl.schedule.task;

import com.yxyl.schedule.common.ScheduleConstants;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.config.CronTask;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @program: starter
 * @description:
 * @author: YxYL
 * @create: 2023-06-27 13:53
 **/
@Component("yxyl-middleware-schedule-cronTaskRegister")
public class CronTaskRegister implements DisposableBean {

    @Resource(name = "yxyl-middleware-schedule-taskSchedule")
    private TaskScheduler taskScheduler;

    public TaskScheduler getTaskScheduler() {
        return this.taskScheduler;
    }


    public void addCronTask(SchedulingRunnable task, String cronExp) {
        if (ScheduleConstants.schedulerTasks.get(task.taskId()) != null) {
            removeCronTask(task.taskId());
        }
        CronTask cronTask = new CronTask(task, cronExp);
        ScheduleConstants.schedulerTasks.put(task.taskId(), scheduleCronTask(cronTask));
    }


    private ScheduledTask scheduleCronTask(CronTask cronTask) {
        ScheduledTask scheduledTask = new ScheduledTask();
        scheduledTask.future = this.taskScheduler.schedule(cronTask.getRunnable(), cronTask.getTrigger());
        return scheduledTask;
    }

    public void removeCronTask(String taskId) {
        ScheduledTask scheduledTask = ScheduleConstants.schedulerTasks.remove(taskId);
        if (scheduledTask == null) {
            return;
        }
        // 如果不为null的话，就取消任务
        scheduledTask.cancel();
    }


    @Override
    public void destroy() {
        for (ScheduledTask task : ScheduleConstants.schedulerTasks.values()) {
            task.cancel();
        }
        ScheduleConstants.schedulerTasks.clear();
    }
}
