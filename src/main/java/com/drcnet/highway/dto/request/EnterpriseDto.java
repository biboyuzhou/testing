package com.drcnet.highway.dto.request;

import com.drcnet.highway.util.validate.AddValid;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/6 16:24
 * @Description:
 */
@Data
public class EnterpriseDto {

    @NotEmpty(message = "企业名称不能为空",groups = AddValid.class)
    private String name;

}
