package com.drcnet.highway.dto.request;

import com.drcnet.highway.util.validate.AddValid;
import com.drcnet.highway.util.validate.MonthValid;
import com.drcnet.highway.util.validate.PageValid;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * @Author: penghao
 * @CreateTime: 2019/1/14 14:43
 * @Description:
 */
@Data
@ToString
public class MonthPageDto {

    @NotNull(message = "开始时间不能为空",groups = {AddValid.class, MonthValid.class})
    Integer beginMonth;
    @NotNull(message = "结束时间不能为空",groups = {AddValid.class, MonthValid.class})
    Integer endMonth;
    @NotNull(message = "页码不能为空",groups = {AddValid.class, PageValid.class})
    Integer pageNum;
    @NotNull(message = "页面大小不能为空",groups = {AddValid.class, PageValid.class})
    Integer pageSize;
    
}
