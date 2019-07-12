package com.drcnet.highway.dto.request;

import com.drcnet.highway.util.validate.AddValid;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/10 9:53
 * @Description:
 */
@Data
public class WhiteDto {

    @NotBlank(message = "车牌号不能为空",groups = AddValid.class)
    private String carNo;
    @NotNull(message = "标记不能为空",groups = AddValid.class)
    private Integer flag;

}
