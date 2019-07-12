package com.drcnet.highway.dto.request;

import com.drcnet.highway.util.validate.PageValid;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/10 15:16
 * @Description:
 */
@Data
public class PagingDto {
    @NotNull(groups = PageValid.class)
    private Integer pageNum;
    @NotNull(groups = PageValid.class)
    private Integer pageSize;

}
