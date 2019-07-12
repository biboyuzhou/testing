package com.drcnet.highway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: penghao
 * @CreateTime: 2019/1/16 9:50
 * @Description: 先进后出时间段数量Dto
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PeriodAmountDto implements Serializable {

    private static final long serialVersionUID = -6496820874295542283L;
    private String period;

    private Integer amount;

}
