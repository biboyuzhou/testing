package com.drcnet.highway.dto.request;

import com.drcnet.highway.util.validate.QueryValid;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/14 13:45
 * @Description:
 */
@Data
public class CarMonthQueryDto extends PagingDto {

    @NotNull(groups = QueryValid.class)
    private Integer beginMonth;
    @NotNull(groups = QueryValid.class)
    private Integer carNoId;
    @NotNull(groups = QueryValid.class)
    private Integer flag;

}
