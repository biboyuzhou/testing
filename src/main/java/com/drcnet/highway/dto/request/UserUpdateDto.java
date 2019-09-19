package com.drcnet.highway.dto.request;

import com.drcnet.highway.util.validate.UpdateValid;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/8 9:25
 * @Description:
 */
@Data
public class UserUpdateDto {

    @NotNull(message = "id不能为空",groups = UpdateValid.class)
    private Integer id;

    private String password;

    private String name;

    private Integer enterpriseId;

    private Integer roleId;

}
