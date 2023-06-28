package com.yxyl.schedule.service;

import com.alibaba.fastjson.JSON;
import com.yxyl.schedule.common.ScheduleConstants;
import com.yxyl.schedule.domain.Instruct;
import com.yxyl.schedule.task.CronTaskRegister;
import com.yxyl.schedule.task.SchedulingRunnable;
import com.yxyl.schedule.util.StrUtil;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.yxyl.schedule.common.ScheduleConstants.Global.*;


/**
 * @program: starter
 * @description: ZK监控客户端
 * @author: YxYL
 * @create: 2023-06-27 21:33
 **/

public class ZkCuratorServer {

    private static final Logger logger = LoggerFactory.getLogger(ZkCuratorServer.class);

    public static CuratorFramework getClint(String connectString) {
        if (ScheduleConstants.Global.client != null) {
            return ScheduleConstants.Global.client;
        }

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        //添加重连监听
        client.getConnectionStateListenable().addListener(((curatorFramework, connectionState) -> {
            switch (connectionState) {
                case CONNECTED:
                    logger.info("middleware schedule init server connected {}", connectString);
                    break;
                case RECONNECTED:
                    break;
                default:
                    break;
            }
        }));
        client.start();
        ScheduleConstants.Global.client = client;
        return client;
    }


    /**
     * 所有子节点监听
     *
     * @param applicationContext
     * @param c1
     * @param client
     * @param path
     * @throws Exception
     */
    public static void addTreeCacheListener(final ApplicationContext applicationContext, final CuratorFramework client, String path) throws Exception {
        TreeCache treeCache = new TreeCache(client, path);
        treeCache.start();
        treeCache.getListenable().addListener((curatorFramework, event) -> {
            if (event.getData() == null) {
                return;
            }
            byte[] eventData = event.getData().getData();
            if (null == eventData || eventData.length < 1) {
                return;
            }
            String json = new String(eventData, ScheduleConstants.Global.CHARSET_NAME);
            if ("".equals(json) || json.indexOf("{") != 0 || json.lastIndexOf("}") + 1 != json.length()) {
                return;
            }
            Instruct instruct = JSON.parseObject(new String(event.getData().getData(), ScheduleConstants.Global.CHARSET_NAME), Instruct.class);
            switch (event.getType()) {
                case NODE_ADDED:
                case NODE_UPDATED:
                    if (ScheduleConstants.Global.ip.equals(instruct.getIp()) && ScheduleConstants.Global.schedulerServerId.equals(instruct.getSchedulerServerId())) {
                        //获取对象
                        CronTaskRegister cronTaskRegister = applicationContext.getBean("yxyl-middleware-schedule-cronTaskRegister", CronTaskRegister.class);
                        boolean isExist = applicationContext.containsBean(instruct.getBeanName());
                        if (!isExist) {
                            return;
                        }
                        Object scheduleBean = applicationContext.getBean(instruct.getBeanName());
                        String path_root_server_ip_clazz_method_status = StrUtil.joinStr(path_root, LINE, "server", LINE, instruct.getSchedulerServerId(), LINE, "ip", LINE, "clazz", LINE, instruct.getBeanName(), LINE, "method", LINE, instruct.getMethodName(), "/status");
                        //执行命令
                        Integer status = instruct.getStatus();
                        switch (status) {
                            case 0:
                                cronTaskRegister.removeCronTask(instruct.getBeanName() + "_" + instruct.getMethodName());
                                setData(client, path_root_server_ip_clazz_method_status, "0");
                                logger.info("middleware schedule task stop {} {}", instruct.getBeanName(), instruct.getMethodName());
                                break;
                            case 1:
                                cronTaskRegister.addCronTask(new SchedulingRunnable(scheduleBean, instruct.getBeanName(), instruct.getMethodName()), instruct.getCron());
                                setData(client, path_root_server_ip_clazz_method_status, "1");
                                logger.info("middleware schedule task start {} {}", instruct.getBeanName(), instruct.getMethodName());
                                break;
                            case 2:
                                cronTaskRegister.removeCronTask(instruct.getBeanName() + "_" + instruct.getMethodName());
                                cronTaskRegister.addCronTask(new SchedulingRunnable(scheduleBean, instruct.getBeanName(), instruct.getMethodName()), instruct.getCron());
                                setData(client, path_root_server_ip_clazz_method_status, "1");
                                logger.info("middleware schedule task refresh {} {}", instruct.getBeanName(), instruct.getMethodName());
                                break;
                        }
                    }
                    break;
                case NODE_REMOVED:
                    break;
                default:
                    break;
            }
        });
    }

    /**
     * 创建节点
     *
     * @param client
     * @param path
     * @throws Exception
     */
    public static void createNode(CuratorFramework client, String path) throws Exception {
        ArrayList<String> pathChild = new ArrayList<>();
        pathChild.add(path);
        if (pathChild.lastIndexOf(LINE) > 0) {
            path = path.substring(0, path.lastIndexOf(LINE));
            pathChild.add(path);
        }
        for (int i = pathChild.size() - 1; i >= 0; i--) {
            Stat stat = client.checkExists().forPath(pathChild.get(i));
            if (stat == null) {
                client.create().creatingParentsIfNeeded().forPath(pathChild.get(i));
            }
        }
    }

    /**
     * 创建节点
     *
     * @param client
     * @param path
     * @throws Exception
     */
    public static void createNodeSimple(CuratorFramework client, String path) throws Exception {
        if (null == client.checkExists().forPath(path)) {
            client.create().creatingParentsIfNeeded().forPath(path);
        }
    }

    /**
     * 删除节点
     *
     * @param client
     * @param path
     * @throws Exception
     */
    private static void deleteNodeSimple(CuratorFramework client, String path) throws Exception {
        if (client.checkExists().forPath(path) != null) {
            client.delete().deletingChildrenIfNeeded().forPath(path);
        }
    }

    /**
     * 设置数据
     *
     * @param client
     * @param path
     * @param data
     * @throws Exception
     */
    public static void setData(CuratorFramework client, String path, String data) throws Exception {
        if (client.checkExists().forPath(path) == null) {
            return;
        }
        client.setData().forPath(path, data.getBytes(CHARSET_NAME));
    }

    /**
     * 获取数据
     *
     * @param client
     * @param path
     * @return
     * @throws Exception
     */
    public static byte[] getData(CuratorFramework client, String path) throws Exception {
        return client.getData().forPath(path);
    }

    /**
     * 删除数据
     *
     * @param client
     * @param path
     * @throws Exception
     */
    public static void deleteDataRetainNode(CuratorFramework client, String path) throws Exception {
        if (client.checkExists().forPath(path) == null) {
            client.delete().forPath(path);
        }
    }

    /**
     * 添加临时节点数据
     *
     * @param client
     * @param path
     * @param data
     * @throws Exception
     */
    public static void appendPersistentData(CuratorFramework client, String path, String data) throws Exception {
        //创建一个持久化的临时节点，数据内容——>字节数组
        PersistentEphemeralNode node = new PersistentEphemeralNode(client, PersistentEphemeralNode.Mode.EPHEMERAL, path, data.getBytes(CHARSET_NAME));
        //启动后，会在zk服务器上穿件一个对应的持久化的临时节点
        node.start();
        //等待3S过后才创建
        node.waitForInitialCreate(3, TimeUnit.SECONDS);
    }

    /**
     * 删除子节点
     *
     * @param client
     * @param path
     * @throws Exception
     */
    public static void deletingChildrenIfNeeded(CuratorFramework client, String path) throws Exception {
        if (client.checkExists().forPath(path) == null) {
            return;
        }
        //递归删除节点
        client.delete().deletingChildrenIfNeeded().forPath(path);
    }
}
