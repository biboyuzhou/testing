package com.drcnet.highway.dto.request;

import com.drcnet.highway.util.validate.QueryValid;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/27 11:17
 * @Description:
 */
@Data
public class CheatingListDto extends PagingDto {

    @NotNull(groups = QueryValid.class)
    private Integer beginMonth;
    @NotNull(groups = QueryValid.class)
    private Integer carType;

    private Integer flag;
    private Integer riskFlag;

    private String flags;

    private List<String> fields;
}
