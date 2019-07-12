package com.drcnet.highway.dto.request;

import com.drcnet.highway.util.validate.AddValid;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/10 15:14
 * @Description:
 */
@Data
public class RiskInOutDto extends PagingDto {

    @NotNull(groups = AddValid.class)
    private Integer carId;
    @NotNull(groups = AddValid.class)
    private String beginMonth;
    @NotNull(groups = AddValid.class)
    private Integer code;

}
