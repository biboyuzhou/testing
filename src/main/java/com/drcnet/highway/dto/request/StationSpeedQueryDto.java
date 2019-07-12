package com.drcnet.highway.dto.request;

import lombok.Data;

/**
 * @Author: penghao
 * @CreateTime: 2019/6/19 11:35
 * @Description: 查询路段速度异常通行记录
 */
@Data
public class StationSpeedQueryDto extends PagingDto{

    private Integer rkId;

    private Integer ckId;

    private Integer vc;

}
