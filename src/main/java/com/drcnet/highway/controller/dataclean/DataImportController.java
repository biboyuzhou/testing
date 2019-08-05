package com.drcnet.highway.controller.dataclean;

import com.drcnet.highway.service.dataclean.DataImportService;
import com.drcnet.highway.util.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author: penghao
 * @CreateTime: 2019/7/23 10:35
 * @Description:
 */
@RequestMapping("dataImport")
@Slf4j
@RestController
@Api(tags = "数据导入接口")
public class DataImportController {

    @Resource
    private DataImportService dataImportService;

    @ApiOperation("导入二绕7月份的数据")
    @GetMapping("importByExcel")
    public Result importByExcel(){
        long timeMillis = System.currentTimeMillis();
        log.info("开始导入数据");
        dataImportService.import2ndRoundData();
        log.info("数据导入成功，耗时:{} 秒",(System.currentTimeMillis() - timeMillis)/1000);
        return Result.ok();
    }

    @ApiOperation("导入二绕入口数据")
    @GetMapping("importInboundDataByExcel")
    public Result importInboundDataByExcel(){
        long timeMillis = System.currentTimeMillis();
        log.info("开始导入入口数据");
        dataImportService.importInboundDataByExcel();
        log.info("数据导入成功，耗时:{} 秒",(System.currentTimeMillis() - timeMillis)/1000);
        return Result.ok();
    }

}
