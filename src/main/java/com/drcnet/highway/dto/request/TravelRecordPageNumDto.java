package com.drcnet.highway.dto.request;

import com.drcnet.highway.util.validate.QueryValid;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Author jack
 * @Date: 2019/8/14 14:22
 * @Desc:
 **/
@Data
public class TravelRecordPageNumDto implements Serializable {
    private static final long serialVersionUID = -8740244913702486936L;

    /**
     * 当前行程记录id
     */
    @NotNull(groups = QueryValid.class)
    private Integer recordId;

    /**
     * 车辆id
     */
    @NotNull(groups = QueryValid.class)
    private Integer carId;

    /**
     * 分页每页数量
     */
    @NotNull(groups = QueryValid.class)
    private Integer pageSize;

    /**
     * 是否是当前路段通行记录的id
     * @see com.drcnet.highway.constants.enumtype.YesNoEnum
     */
    @NotNull(groups = QueryValid.class)
    private Integer isCurrent;


}
