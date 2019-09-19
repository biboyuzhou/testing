package com.drcnet.highway.controller;

import com.drcnet.highway.constants.ModuleConsts;
import com.drcnet.highway.dto.request.PagingDto;
import com.drcnet.highway.service.DataImportTaskService;
import com.drcnet.highway.util.Result;
import com.drcnet.highway.util.validate.PageValid;
import com.drcnet.usermodule.annotation.PermissionCheck;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/15 9:06
 * @Description:
 */
@RestController
@RequestMapping("dataImportTask")
@Api(tags = "数据上传日志接口")
@Slf4j
public class DataImportTaskController {

    @Resource
    private DataImportTaskService dataImportTaskService;

    @ApiOperation(value = "获得数据上传日志",notes = "返回结果，state:1已上传，2导入中，3已导入，4导入失败<br>" +
            "boundType:0进站数据，1出站数据，2计算任务<br>createTime：上传时间,finishTime：数据导入完成时间<br>" +
            "successAmount:成功导入记录数,failureAmount:失败导入记录数,repeateAmount:重复数据条数<br>" +
            "errMsg:错误信息,username:上传者用户名")
    @GetMapping("listDataImportTaskLog")
    @PermissionCheck(ModuleConsts.UPLOAD_DATA)
    public Result listDataImportTaskLog(@Validated(PageValid.class) PagingDto pagingDto){
        return Result.ok(dataImportTaskService.listDataImportTaskLog(pagingDto));
    }

}
