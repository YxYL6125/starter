package com.yxyl.schedule.domain;

import java.util.List;
import java.util.Map;

/**
 * @program: starter
 * @description:
 * @author: YxYL
 * @create: 2023-06-27 10:49
 **/

public class DcsServerNode {

    private String schedulerServerId;
    private String schedulerServerName;

    public DcsServerNode() {
    }

    public DcsServerNode(String schedulerServerId, String schedulerServerName) {
        this.schedulerServerId = schedulerServerId;
        this.schedulerServerName = schedulerServerName;
    }

    public String getSchedulerServerId() {
        return schedulerServerId;
    }

    public void setSchedulerServerId(String schedulerServerId) {
        this.schedulerServerId = schedulerServerId;
    }

    public String getSchedulerServerName() {
        return schedulerServerName;
    }

    public void setSchedulerServerName(String schedulerServerName) {
        this.schedulerServerName = schedulerServerName;
    }
}
