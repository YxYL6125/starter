package com.yxyl.schedule.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @program: starter
 * @description: 配置
 * @author: YxYL
 * @create: 2023-06-27 20:55
 **/
@Configuration("yxyl-middleware-schedule-starterAutoConfig")
@EnableConfigurationProperties()
public class StarterAutoConfig {
    
    @Resource
    private StarterServiceProperties properties;

    public StarterServiceProperties getProperties() {
        return properties;
    }

    public void setProperties(StarterServiceProperties properties) {
        this.properties = properties;
    }
}
