package com.drcnet.highway.dto.request;

import com.drcnet.highway.util.validate.AddValid;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/14 11:21
 * @Description:
 */
@Data
public class DetailMarkDto {

    @NotNull(groups = AddValid.class)
    private Integer id;
    @NotNull(groups = AddValid.class)
    private String monthTime;
    @NotNull(groups = AddValid.class)
    private Boolean flag;

}
