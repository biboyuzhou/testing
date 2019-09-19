package com.drcnet.highway.dto.response;

import com.drcnet.highway.dto.PeriodAmountDto;
import lombok.Data;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/16 20:10
 * @Description:
 */
@Data
public class TrafficStatisticsVo extends PeriodAmountDto {

    private Integer inAmount;

}
