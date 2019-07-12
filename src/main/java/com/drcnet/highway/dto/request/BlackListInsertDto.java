package com.drcnet.highway.dto.request;

import com.drcnet.highway.util.validate.AddValid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/13 14:16
 * @Description: 添加黑名单dto
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlackListInsertDto {

    @NotBlank(groups = AddValid.class)
    private String carNo;
    @NotNull(groups = AddValid.class)
    private Integer cheating;
    @NotNull(groups = AddValid.class)
    private Integer violation;
    @NotNull(groups = AddValid.class)
    private Integer score;
    @NotNull(groups = AddValid.class)
    private Integer flag;

}
