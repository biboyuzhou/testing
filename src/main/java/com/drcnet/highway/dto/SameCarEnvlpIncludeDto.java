package com.drcnet.highway.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: penghao
 * @CreateTime: 2019/6/13 15:48
 * @Description:
 */
@Data
public class SameCarEnvlpIncludeDto implements Serializable {

    private static final long serialVersionUID = -9085230902438217277L;

    private Integer vlpId;

    private String vlp;

    private List<SameCarEnvlpDto> envlps;

}
