package com.drcnet.highway.dto.request;

import com.drcnet.highway.constants.TipsConsts;
import com.drcnet.highway.util.validate.AddValid;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/7 16:45
 * @Description:
 */
@Data
public class AllotRoleDto {

    @NotNull(message = TipsConsts.LACK_PARAMS,groups = AddValid.class)
    private Integer userId;

    @NotNull(message = TipsConsts.LACK_PARAMS,groups = AddValid.class)
    private Integer roleId;

}
