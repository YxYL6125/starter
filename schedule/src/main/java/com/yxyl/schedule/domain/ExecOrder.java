package com.yxyl.schedule.domain;


import com.alibaba.fastjson.annotation.JSONField;

/**
 * @program: starter
 * @description:
 * @author: YxYL
 * @create: 2023-06-27 10:50
 **/

public class ExecOrder {
    
    @JSONField(serialize = false)
    private Object bean;
    private String beanName;
    private String methodName;
    private String desc;
    private String cron;
    private Boolean autoStartup;


    public ExecOrder() {
    }

    public ExecOrder(Object bean, String beanName, String methodName, String desc, String cron, Boolean autoStartup) {
        this.bean = bean;
        this.beanName = beanName;
        this.methodName = methodName;
        this.desc = desc;
        this.cron = cron;
        this.autoStartup = autoStartup;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
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

    public Boolean getAutoStartup() {
        return autoStartup;
    }

    public void setAutoStartup(Boolean autoStartup) {
        this.autoStartup = autoStartup;
    }
}
