package com.drcnet.highway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/14 18:37
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuccessAmountDto {

    private Integer total;

    private Integer success;

}
