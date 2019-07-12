package com.drcnet.highway.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: penghao
 * @CreateTime: 2019/6/13 15:37
 * @Description:
 */
@Data
public class SameCarEnvlpDto implements Serializable {

    private static final long serialVersionUID = 9031044326347126300L;
    private Integer envlpId;

    private String envlp;
    private String vlp;

    private Integer amount;
}
