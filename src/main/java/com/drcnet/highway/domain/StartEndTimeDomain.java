package com.drcnet.highway.domain;

import com.drcnet.highway.constants.TimeConsts;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author jack
 * @Date: 2019/9/12 9:51
 * @Desc:
 **/
@Data
public class StartEndTimeDomain {

    /**
     * 进站时间
     * @DateTimeFormat(pattern = TimeConsts.TIME_FORMAT)  添加该注解，可以对前端传入的数据进行格式化
     */
    @JsonFormat(pattern = TimeConsts.TIME_FORMAT,timezone = TimeConsts.GMT8)
    private LocalDateTime maxTime;

    /**
     * 进站时间
     */
    @JsonFormat(pattern = TimeConsts.TIME_FORMAT,timezone = TimeConsts.GMT8)
    private LocalDateTime minTime;
}
