package com.drcnet.highway.dto;

import com.drcnet.highway.dto.request.MonthPageDto;
import lombok.Data;
import lombok.ToString;

/**
 * @Author: penghao
 * @CreateTime: 2019/1/21 11:12
 * @Description:
 */
@Data
@ToString(callSuper = true)
public class SearchPageCondition extends MonthPageDto {

    private Integer inCarNoId;

    private Integer outCarNoId;

    private String inCarNo;

    private String outCarNo;



}
