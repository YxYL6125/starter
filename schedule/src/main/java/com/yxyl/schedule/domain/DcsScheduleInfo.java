package com.yxyl.schedule.domain;

/**
 * @program: starter
 * @description:
 * @author: YxYL
 * @create: 2023-06-27 10:47
 **/

public class DcsScheduleInfo {

    private String ip;
    private String schedulerServerId;
    private String schedulerServerName;
    private String beanName;
    private String methodName;
    private String desc;
    private String cron;
    private Integer status;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
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

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "DcsScheduleInfo{" +
                "ip='" + ip + '\'' +
                ", schedulerServerId='" + schedulerServerId + '\'' +
                ", schedulerServerName='" + schedulerServerName + '\'' +
                ", beanName='" + beanName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", desc='" + desc + '\'' +
                ", cron='" + cron + '\'' +
                ", status=" + status +
                '}';
    }
}
