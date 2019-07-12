package com.drcnet.highway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: penghao
 * @CreateTime: 2019/1/17 11:41
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmountRateDto {
    //数量
    private Integer amount;
    //占比
    private Double rate;

}
