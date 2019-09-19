package com.drcnet.highway.entity;

import com.drcnet.highway.config.StandardParam;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Map;

@Slf4j
@Data
@Table(name = "tietou_feature_extraction")
public class TietouFeatureExtraction implements Serializable {
    /**
     * 行驶记录编号
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    /**
     * 月份
     */
    @Column(name = "month_time")
    private Integer monthTime;

    /**
     * 车牌号
     */
    @Column(name = "vlp")
    private String vlp;

    @Column(name = "vlp_id")
    private Integer vlpId;

    /**
     * 是否同站进出（1表示是）
     */
    @Column(name = "same_station")
    private Integer sameStation;

    /**
     * 平均速度
     */
    private Integer speed;

    @Column(name = "low_speed")
    private Integer lowSpeed;

    @Column(name = "high_speed")
    private Integer highSpeed;
    /**
     * 进出车牌是否相同（相同则为1）
     */
    @Column(name = "same_car_number")
    private Integer sameCarNumber;

    /**
     * 进出车型是否一致（一致则为1）
     */
    @Column(name = "same_car_type")
    private Integer sameCarType;

    /**
     * 车情是否一致（一致则为1）
     */
    @Column(name = "same_car_situation")
    private Integer sameCarSituation;

    /**
     * 是否短途重载
     */
    @Column(name = "short_dis_overweight")
    private Integer shortDisOverweight;

    /**
     * 是否长途轻载
     */
    @Column(name = "long_dis_lightweight")
    private Integer longDisLightweight;

    /**
     * 该车牌是否有不同的轴数
     */
    @Column(name = "different_zhou")
    private Integer differentZhou;

    /**
     * 标志与真实标志是否一致
     */
    @Column(name = "diff_flagstation_info")
    private Integer diffFlagstationInfo;

    /**
     * 相同时间段是否同时出现该车牌号
     */
    @Column(name = "same_time")
    private Integer sameTime;

    /**
     * 手动标记结果，一为疑似逃费车辆
     */
    private Integer label;

    @Column(name = "min_out_in")
    private Integer minOutIn;

    @Column(name = "same_time_range_again")
    private Integer sameTimeRangeAgain;

    @Column(name = "flagstation_lost")
    private Integer flagstationLost;

    private String sameRouteMark;

    @Column(name = "rk_id")
    private Integer rkId;

    @Column(name = "ck_id")
    private Integer ckId;
    /**
     * 用于模型训练完成后的打分
     */
    private Integer score;

    /**
     * 0货车，1客车
     */
    @Column(name = "car_type")
    private Integer carType;

    private static final long serialVersionUID = 1L;

    public void standard() {
        Class<? extends TietouFeatureExtraction> aClass = this.getClass();
        try {
            for (Map.Entry<String, StandardParam.FeatureStandard> standardEntry : StandardParam.featureStandardMap.entrySet()) {
                String key = standardEntry.getKey();
                StandardParam.FeatureStandard value = standardEntry.getValue();
                Field field = aClass.getDeclaredField(key);
                Integer feature = (Integer)field.get(this);
                Integer finalScore = value.getScore(feature);
                field.set(this,finalScore);
            }
        }catch (ReflectiveOperationException e){
            log.error("{}",e);
            throw new IllegalArgumentException();
        }



    }
}