package com.drcnet.highway.dto.request;

import com.drcnet.highway.constants.TipsConsts;
import com.drcnet.highway.util.validate.AddValid;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/7 16:05
 * @Description:
 */
@Data
public class LoginDto {

    @NotEmpty(message = TipsConsts.LACK_PARAMS,groups = AddValid.class)
    private String username;
    @NotEmpty(message = TipsConsts.LACK_PARAMS,groups = AddValid.class)
    private String password;
}
