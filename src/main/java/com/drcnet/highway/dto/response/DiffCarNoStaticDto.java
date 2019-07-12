package com.drcnet.highway.dto.response;

import com.drcnet.highway.vo.PageVo;
import lombok.Data;

import java.util.List;

/**
 * @Author jack
 * @Date: 2019/6/19 9:45
 * @Desc: 统计指定车辆的进出车牌不一致数据出参
 **/
@Data
public class DiffCarNoStaticDto {
    /**
     * 进站车牌统计数据，取前10条
     */
    private List<DiffCarNoEnvlpDto> envlpDtoList;

    private PageVo<DiffCarNoInOutDataDto> diffCarNoInOutDataDtoPageVo;
}
