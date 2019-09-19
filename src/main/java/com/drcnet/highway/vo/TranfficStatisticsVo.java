package com.drcnet.highway.vo;

import com.drcnet.highway.constants.TimeConsts;
import com.drcnet.highway.dto.response.TrafficStatisticsVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/15 10:45
 * @Description:
 */
@Data
public class TranfficStatisticsVo {

    private List<TrafficStatisticsVo> amountList;

    @JsonFormat(pattern = TimeConsts.DATE_FORMAT,timezone = TimeConsts.GMT8)
    private LocalDate beginDate;

    @JsonFormat(pattern = TimeConsts.DATE_FORMAT,timezone = TimeConsts.GMT8)
    private LocalDate endDate;

}
