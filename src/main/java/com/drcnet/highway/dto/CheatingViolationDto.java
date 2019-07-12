package com.drcnet.highway.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/13 9:28
 * @Description:
 */
@NoArgsConstructor
@Data
public class CheatingViolationDto {

    private Integer carNoId;

    private String carNo;

    private BigDecimal cheating;

    private BigDecimal violation;

    private BigDecimal score;

}
