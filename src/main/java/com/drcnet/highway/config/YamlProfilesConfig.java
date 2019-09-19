package com.drcnet.highway.config;

import org.yaml.snakeyaml.Yaml;

import java.util.Map;

/**
 * @Author: penghao
 * @CreateTime: 2019/9/2 16:48
 * @Description: 用于获得当前yml中profile的字符串属性，例如spring-${profile}.yml,
 */
public class YamlProfilesConfig {

    public static String PROFILE;

    public static void initArgs(String[] args) {
        String param = null;
        if (args != null && args.length >0){
            for (String arg : args) {
                if (arg.startsWith("--spring.profiles.active")){
                    param = arg.split("=")[1];
                    break;
                }
            }
        }
        if (param == null){
            Yaml yaml = new Yaml();
            Map map = yaml.loadAs(LogbackDirConfig.class.getResourceAsStream("/application.yml"), Map.class);
            Map springMap = (Map) map.get("spring");
            Map profiles = (Map) springMap.get("profiles");
            param = (String) profiles.get("active");
        }
        if (param.contains(",")){
            String[] split = param.split(",");
            param = split[split.length - 1];
        }
        PROFILE = param;
    }

    private YamlProfilesConfig() {
    }
}
