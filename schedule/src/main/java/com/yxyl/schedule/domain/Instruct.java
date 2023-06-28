package com.yxyl.schedule.domain;

/**
 * @program: starter
 * @description:
 * @author: YxYL
 * @create: 2023-06-27 10:52
 **/

public class Instruct {
    private String ip;
    private String schedulerServerId;
    private String beanName;
    private String methodName;
    private String cron;
    private Integer status;


    public Instruct() {
    }

    public Instruct(String ip, String schedulerServerId, String beanName, String methodName, String cron, Integer status) {
        this.ip = ip;
        this.schedulerServerId = schedulerServerId;
        this.beanName = beanName;
        this.methodName = methodName;
        this.cron = cron;
        this.status = status;
    }

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
}
