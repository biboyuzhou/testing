package com.drcnet.highway.dto.response;

import lombok.Data;

/**
 * @Author jack
 * @Date: 2019/6/18 16:08
 * @Desc: 时间重叠风险重叠总数及最大重叠次数统计
 **/
@Data
public class SameTimeRangeStaticDto {

    /**
     * 总的时间重叠id数
     */
    private Integer totalNum;

    /**
     * 相同id重叠的最大数量
     */
    private Integer maxNum;
}
