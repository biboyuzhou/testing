package com.drcnet.highway.dto.request;

import com.drcnet.highway.constants.TipsConsts;
import com.drcnet.highway.util.validate.AddValid;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 * @Author: penghao
 * @CreateTime: 2019/7/29 11:16
 * @Description:
 */
@Data
public class UserDto {

    @NotEmpty(message = TipsConsts.LACK_PARAMS, groups = AddValid.class)
    @Size(message = "用户名最多16位", max = 16)
    private String username;
    @NotEmpty(message = TipsConsts.LACK_PARAMS, groups = AddValid.class)
    @Size(message = "密码最多16位", max = 16)
    private String password;

    private String name;

    private Integer roleId;

    private Integer enterpriseId;

}
