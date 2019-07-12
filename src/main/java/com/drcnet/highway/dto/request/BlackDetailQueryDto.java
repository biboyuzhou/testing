package com.drcnet.highway.dto.request;

import lombok.Data;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/14 9:13
 * @Description:
 */
@Data
public class BlackDetailQueryDto extends PagingDto{

    private String carNo;

    private String enTime;

    private String exTime;

    private String oper;

}
