package com.drcnet.highway.dto.request;

import com.drcnet.highway.util.validate.AddValid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

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
    private Integer cheating;
    private Integer violation;
    @NotNull(groups = AddValid.class)
    private BigDecimal score;
    /**
     * 添加黑名单或者删除黑名单 1 添加 0 删除
     */
    @NotNull(groups = AddValid.class)
    private Integer flag;

    /**
     * 风险项，多个风险项,号隔开
     * @see com.drcnet.highway.constants.enumtype.RiskFlagEnum
     */
    @NotNull(groups = AddValid.class)
    private String riskFlag;

    /**
     * 疑似逃费行为，多个行为,逗号隔开
     * @see com.drcnet.highway.constants.enumtype.EscapeBehaviorEnum
     */
    @NotNull(groups = AddValid.class)
    private String escapeBehavior;

    /**
     * 逃费行为描述
     */
    private String description;

}
