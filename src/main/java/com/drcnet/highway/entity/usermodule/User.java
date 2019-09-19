package com.drcnet.highway.entity.usermodule;

import com.drcnet.highway.constants.TimeConsts;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 用户名
     */
    private String username;

    private String name;

    /**
     * 密码
     */
    private String password;

    private String salt;

    @DateTimeFormat(pattern = TimeConsts.TIME_FORMAT_SIMPLE)
    @JsonFormat(pattern = TimeConsts.TIME_FORMAT_SIMPLE,timezone = TimeConsts.GMT8)
    @Column(name = "create_time")
    private LocalDateTime createTime;

    @DateTimeFormat(pattern = TimeConsts.TIME_FORMAT_SIMPLE)
    @JsonFormat(pattern = TimeConsts.TIME_FORMAT_SIMPLE,timezone = TimeConsts.GMT8)
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 所属企业ID
     */
    @Column(name = "enterprise_id")
    private Integer enterpriseId;

    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "use_flag")
    private Boolean useFlag;

    @Transient
    private List<String> permissions;

    private static final long serialVersionUID = 1L;

}