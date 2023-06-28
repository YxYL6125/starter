package com.yxyl.schedule.util;

import org.apache.commons.lang.text.StrBuilder;

/**
 * @program: starter
 * @description: 自定义字符串工具类
 * @author: YxYL
 * @create: 2023-06-27 10:54
 **/

public class StrUtil {
    
    
    
    public static String joinStr(String... str) {
        var sb = new StrBuilder();
        for (String sign : str) {
            if (sign != null) {
                sb.append(sign);
            }
        }
        return sb.toString();
    }
}
