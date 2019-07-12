package com.drcnet.highway.vo;

import com.drcnet.highway.dto.PeriodAmountDto;
import com.drcnet.highway.dto.TurnoverStationDto;
import com.drcnet.highway.entity.TietouFeatureExtractionStandardScore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/8 19:09
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnoverVo {

    private List<TurnoverStationDto> stationTurnovers;

    private Integer carType;

    private String carNo;

    private Integer type;

    private Integer axlenum;

    private Integer throughAmount;

    private TietouFeatureExtractionStandardScore violationScore;

    private List<PeriodAmountDto> periodAmount;
}
