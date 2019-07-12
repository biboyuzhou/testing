package com.drcnet.highway.vo;

import com.drcnet.highway.dto.PeriodAmountDto;
import lombok.Data;

import java.util.List;

/**
 * @Author: penghao
 * @CreateTime: 2019/6/19 14:54
 * @Description:
 */
@Data
public class CarTypeDistributionVo {

    private List<PeriodAmountDto> inType;

    private List<PeriodAmountDto> outType;

}
