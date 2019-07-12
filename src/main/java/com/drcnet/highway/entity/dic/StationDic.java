package com.drcnet.highway.entity.dic;

import lombok.Data;

import java.io.Serializable;
import javax.persistence.*;

@Data
@Table(name = "station_dic")
public class StationDic implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 收费站点名
     */
    @Column(name = "station_name")
    private String stationName;

    /**
     * 坐标，逗号分割
     */
    private String coordinate;

    /**
     * 所属省份Id
     */
    @Column(name = "region_id")
    private Integer regionId;

    private static final long serialVersionUID = 1L;

}