package com.drcnet.highway.config;

import ch.qos.logback.core.PropertyDefinerBase;
import org.springframework.stereotype.Component;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/20 11:05
 * @Description: 用来对日志分包
 */
@Component
public class LogbackDirConfig extends PropertyDefinerBase {



    @Override
    public String getPropertyValue() {
        return "log/" + YamlProfilesConfig.PROFILE;
    }
}
