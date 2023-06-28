package com.yxyl.schedule.domain;

/**
 * @program: starter
 * @description:
 * @author: YxYL
 * @create: 2023-06-27 10:46
 **/

public class DataCollect {

    private int ipCount;
    private int serverCount;
    private int beanCount;
    private int methodCount;

    public DataCollect() {
    }

    public int getIpCount() {
        return ipCount;
    }

    public void setIpCount(int ipCount) {
        this.ipCount = ipCount;
    }

    public int getServerCount() {
        return serverCount;
    }

    public void setServerCount(int serverCount) {
        this.serverCount = serverCount;
    }

    public int getBeanCount() {
        return beanCount;
    }

    public void setBeanCount(int beanCount) {
        this.beanCount = beanCount;
    }

    public int getMethodCount() {
        return methodCount;
    }

    public void setMethodCount(int methodCount) {
        this.methodCount = methodCount;
    }

    public DataCollect(int ipCount, int serverCount, int beanCount, int methodCount) {
        this.ipCount = ipCount;
        this.serverCount = serverCount;
        this.beanCount = beanCount;
        this.methodCount = methodCount;
    }
}
