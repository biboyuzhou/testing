package com.drcnet.highway.dto.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static com.drcnet.highway.constants.TimeConsts.DATE_FORMAT;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/15 10:20
 * @Description:
 */
@Data
public class DateSearchDto {

    @DateTimeFormat(pattern = DATE_FORMAT)
    private LocalDate beginDate;

    @DateTimeFormat(pattern = DATE_FORMAT)
    private LocalDate endDate;

}
