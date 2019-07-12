package com.drcnet.highway.dto;

import com.drcnet.highway.dto.request.MonthPageDto;
import lombok.Data;

@Data
public class CarNoDto extends MonthPageDto {
    //车牌号
    private String carNo;

    //是否有处罚记录
    private Integer hasRecord;

}
