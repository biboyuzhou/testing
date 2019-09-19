package com.drcnet.highway.entity;

import com.drcnet.highway.constants.TimeConsts;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table(name = "data_import_task")
public class DataImportTask implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 相对路径
     */
    private String path;

    /**
     * 状态:1已上传，2导入中，3已导入，4导入失败
     */
    private Integer state;

    private String md5;

    @Column(name = "bound_type")
    private Integer boundType;

    @JsonFormat(pattern = TimeConsts.TIME_FORMAT,timezone = TimeConsts.GMT8)
    @Column(name = "create_time")
    private LocalDateTime createTime;

    @JsonFormat(pattern = TimeConsts.TIME_FORMAT,timezone = TimeConsts.GMT8)
    @Column(name = "finish_time")
    private LocalDateTime finishTime;

    @Column(name = "success_amount")
    private Integer successAmount;

    @Column(name = "failure_amount")
    private Integer failureAmount;

    @Column(name = "repeat_amount")
    private Integer repeatAmount;

    @Column(name = "error_msg")
    private String errorMsg;

    @Column(name = "user_id")
    private Integer userId;

    private String filename;

    @Column(name = "statistic_flag")
    private Boolean statisticFlag;

    @Transient
    private String username;

    private static final long serialVersionUID = 1L;

}