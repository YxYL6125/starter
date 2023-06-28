## Schedule

> 基本功能实现；任务接入、分布式启停(多机器部署任务)

### 使用参考：

1. Java运行环境必须高于**openjdk19**
2. springboot 2.x
3. IDEA中设置语言版本为`19(Preview)`
   或者可以添加JVM启动参数：`--enable-preview`
4. 准备好ZK环境





### 使用步骤：

添加依赖

```xml
<dependency>
        <groupId>io.github.yxyl6125</groupId>
        <artifactId>schedule</artifactId>
        <version>1.0.0</version>
</dependency>
```

配置文件：`application.yaml`文件中增加如下配置

```yml
yxyl:
  middleware:
    schedule:
      zk-address: ${host}:${port}
      scheduler-server-id: SchedulerDemo
      scheduler-server-name: 分布式任务测试
```



在启动类上加上注解：`@EnableScheduling`

在任务方法上添加注解：`@DcsScheduled(cron = "0/2 * * * * ?", desc = "01定时任务执行测试：taskMethodGetAllCount")`

> 参数含义：
>
> 1. cron：执行计划
> 2. desc：任务描述
> 3. autoStartup：默认启动状态



### 特点：

1. 其执行定时任务的线程使用的是虚拟线程池
2. 底层载体线程为fork-join-pool
3. ZK服务监听器使用虚拟线程池(有点为了使用而使用了 :cry:)





### TODO

- 集成Nacos，实现多策略注册中心

