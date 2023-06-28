package com.yxyl.schedule.task;

import java.util.concurrent.ScheduledFuture;

/**
 * @program: starter
 * @description:
 * @author: YxYL
 * @create: 2023-06-27 14:02
 **/

public class ScheduledTask {

    volatile ScheduledFuture<?> future;

    /**
     * 取消定时任务
     */
    public void cancel() {
        ScheduledFuture<?> future = this.future;
        if (future == null) {
            return;
        }
        future.cancel(true);
    }

    public boolean isCancelled() {
        ScheduledFuture<?> future = this.future;
        if (future == null) {
            return true;
        }

        return future.isCancelled();
    }
}
