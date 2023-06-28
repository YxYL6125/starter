package com.yxyl.schedule.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @program: starter
 * @description: 属性类
 * @author: YxYL
 * @create: 2023-06-27 20:57
 **/
@Component
@ConfigurationProperties("yxyl.middleware.schedule")
public class StarterServiceProperties {
    
    private String zkAddress;
    private String schedulerServerId;
    private String schedulerServerName;

    public String getZkAddress() {
        return zkAddress;
    }

    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
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
