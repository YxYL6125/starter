package com.yxyl.whitelist.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("yxyl.whitelist")
public class WhiteListProperties {
    private String users;

    public String getUsers() {
        return users;
    }

    public void setUsers(String users) {
        this.users = users;
    }
}
