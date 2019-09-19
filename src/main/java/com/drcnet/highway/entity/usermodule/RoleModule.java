package com.drcnet.highway.entity.usermodule;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Table(name = "role_module")
public class RoleModule implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "module_id")
    private Integer moduleId;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "use_flag")
    private Boolean useFlag;

    private static final long serialVersionUID = 1L;

}