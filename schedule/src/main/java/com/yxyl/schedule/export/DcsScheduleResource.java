package com.yxyl.schedule.export;

import com.alibaba.fastjson.JSON;
import com.yxyl.schedule.domain.*;
import com.yxyl.schedule.util.StrUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.yxyl.schedule.common.ScheduleConstants.Global.*;

/**
 * @program: starter
 * @description:
 * @author: YxYL
 * @create: 2023-06-28 13:53
 **/

public class DcsScheduleResource {

    private CuratorFramework client;

    public DcsScheduleResource() {
    }

    public DcsScheduleResource(String waAddress) {
        client = CuratorFrameworkFactory.newClient(waAddress, new RetryNTimes(10, 5000));
        client.start();
    }

    public CuratorFramework getClient() {
        return client;
    }

    public List<String> getChildren(String path) throws Exception {
        return client.getChildren().forPath(path);
    }

    public int getChildrenCount(String path) throws Exception {
        return client.getChildren().forPath(path).size();
    }

    public String getData(String path) throws Exception {
        byte[] bytes = client.getData().forPath(path);
        if (null == bytes || bytes.length == 0) {
            return null;
        }
        return new String(bytes);
    }

    public void setData(String path, Object data) throws Exception {
        if (null == client.checkExists().forPath(path)) {
            return;
        }
        client.setData().forPath(path, JSON.toJSONString(data).getBytes(CHARSET_NAME));
    }

    public List<String> queryPathRootServerList() throws Exception {
        return getChildren(StrUtil.joinStr(path_root, LINE, "server"));
    }

    public List<String> queryPathRootServerIpList(String schedulerServerId) throws Exception {
        return getChildren(StrUtil.joinStr(path_root, LINE, "server", LINE, schedulerServerId, LINE, "ip"));
    }

    public List<String> queryPathRootServerIpClazz(String schedulerServerId, String ip) throws Exception {
        return getChildren(StrUtil.joinStr(path_root, LINE, "server", LINE, schedulerServerId, LINE, "ip", LINE, ip, LINE, "clazz"));
    }

    public List<String> queryPathRootServerIpClazzMethod(String schedulerServerId, String ip, String clazz) throws Exception {
        return getChildren(StrUtil.joinStr(path_root, LINE, "server", LINE, schedulerServerId, LINE, "ip", LINE, ip, LINE, "clazz", LINE, "method"));
    }

    public ExecOrder queryExecOrder(String schedulerServerId, String ip, String clazz, String method) throws Exception {
        String path = StrUtil.joinStr(path_root, LINE, "server", LINE, schedulerServerId, LINE, "ip", LINE, ip, LINE, "clazz", clazz, LINE, "method", method, LINE, "value");
        if (client.checkExists().forPath(path) == null) {
            return null;
        }
        String objJson = getData(path);
        if (objJson == null) {
            return null;
        }
        return JSON.parseObject(objJson, ExecOrder.class);
    }

    private boolean queryStatus(String schedulerServerId, String ip, String clazz, String method) throws Exception {
        String path = StrUtil.joinStr(path_root, LINE, "server", LINE, schedulerServerId, LINE, "ip", LINE, ip, LINE, "clazz", clazz, LINE, "method", method, LINE, "status");
        String statusStr = getData(path);
        return "1".equals(statusStr);
    }

    public List<DcsScheduleInfo> queryDcsScheduleInfoList(String schedulerServerId) throws Exception {
        ArrayList<DcsScheduleInfo> dcsScheduleInfoList = new ArrayList<>();
        String path_root_server = StrUtil.joinStr(path_root, LINE, "server", LINE, schedulerServerId);
        String schedulerServerName = getData(path_root_server);
        //查询封装结果集
        List<String> ipList = queryPathRootServerIpList(schedulerServerId);
        for (String ip : ipList) {
            List<String> clazzList = queryPathRootServerIpClazz(schedulerServerId, ip);
            for (String clazz : clazzList) {
                List<String> methodList = queryPathRootServerIpClazzMethod(schedulerServerId, ip, clazz);
                for (String method : methodList) {
                    ExecOrder execOrder = queryExecOrder(schedulerServerId, ip, clazz, method);
                    //封装对象
                    DcsScheduleInfo info = new DcsScheduleInfo();
                    info.setIp(ip);
                    info.setSchedulerServerId(schedulerServerId);
                    info.setSchedulerServerName(schedulerServerName);
                    info.setBeanName(clazz);
                    info.setMethodName(method);
                    if (execOrder != null) {
                        info.setDesc(execOrder.getDesc());
                        info.setCron(execOrder.getCron());
                        info.setStatus(queryStatus(schedulerServerId, ip, clazz, method) ? 1 : 0);
                    } else {
                        info.setStatus(2);
                    }
                    dcsScheduleInfoList.add(info);
                }
            }
        }
        return dcsScheduleInfoList;
    }

    public DataCollect queryDataCollect() throws Exception {
        List<String> serverList = queryPathRootServerList();
        AtomicInteger
                ipCount = new AtomicInteger(0),
                serverCount = new AtomicInteger(serverList.size()),
                beanCount = new AtomicInteger(0),
                methodCount = new AtomicInteger(0);
        for (String schedulerServerId : serverList) {
            List<String> ipList = queryPathRootServerIpList(schedulerServerId);
            ipCount.addAndGet(ipList.size());
            for (String ip : ipList) {
                List<String> clazzList = queryPathRootServerIpClazz(schedulerServerId, ip);
                beanCount.addAndGet(clazzList.size());
                for (String clazz : clazzList) {
                    List<String> methodList = queryPathRootServerIpClazzMethod(schedulerServerId, ip, clazz);
                    methodCount.addAndGet(methodList.size());
                }
            }
        }
        return new DataCollect(ipCount.get(), serverCount.get(), beanCount.get(), methodCount.get());
    }

    public List<DcsServerNode> queryScsServerNodeList() throws Exception {
        ArrayList<DcsServerNode> dcsServerNodeList = new ArrayList<>();
        List<String> serverList = queryPathRootServerList();
        for (String schedulerServerId : serverList) {
            String path = StrUtil.joinStr(path_root, LINE, "server", LINE, schedulerServerId);
            String schedulerServerName = getData(path);
            DcsServerNode node = new DcsServerNode(schedulerServerId, schedulerServerName);
            dcsServerNodeList.add(node);
        }
        return dcsServerNodeList;
    }

    public void pushInstruct(Instruct instruct) throws Exception {
        setData("com/yxyl/schedule/exec", instruct);
    }

}
