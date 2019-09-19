package com.drcnet.highway.dto.request;

import lombok.Data;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/12 15:57
 * @Description:
 */
@Data
public class UserQueryDto extends PagingDto {

    private String username;

    private String name;

}
