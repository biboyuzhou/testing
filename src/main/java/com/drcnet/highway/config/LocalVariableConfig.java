package com.drcnet.highway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author jack
 * @Date: 2019/8/7 13:50
 * @Desc:
 **/
@Component
@ConfigurationProperties(prefix = "local")
@Setter
@Getter
public class LocalVariableConfig {

    /**
     * 该条高速路站点id集合
     */
    private List<Integer> stationIdList;

    /**
     * 该条高速路station_dic表mark值
     */
    private Integer enterpriseCode;

    /**
     * 首页站点间相互通行关系图缓存key
     */
    private String relationCacheKey;

    /**
     * 首页地图缓存key
     */
    private String riskMapCacheKey;

    //----------------------定时计算配置相关---------------------
    /**
     * 算法脚本路径
     */
    private String scriptPath;

    /**
     * 前一次数据计算tietou最大的id
     */
    private String previousTietouId;

    /**
     * 前一次数据计算carDic最大id
     */
    private String previousCarId;

    /**
     * 前一次数据计算carDic最大id
     */
    private String previousEndMonth;

    private String roadName;
}
