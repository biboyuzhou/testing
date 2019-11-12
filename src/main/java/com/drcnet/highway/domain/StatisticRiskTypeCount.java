package com.drcnet.highway.domain;

import lombok.Data;

/**
 * @Author jack
 * @Date: 2019/10/8 15:37
 * @Desc:
 **/
@Data
public class StatisticRiskTypeCount {

    private Integer same_station;
    private Integer low_speed;
    private Integer high_speed;
    private Integer same_car_number;
    private Integer same_car_type;
    private Integer same_car_situation;
    private Integer short_dis_overweight;
    private Integer long_dis_lightweight;
    private Integer different_zhou;
    private Integer diff_flagstation_info;
    private Integer min_out_in;
    private Integer same_time_range_again;
    private Integer flagstation_lost;



}
