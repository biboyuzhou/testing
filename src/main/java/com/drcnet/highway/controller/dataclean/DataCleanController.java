package com.drcnet.highway.controller.dataclean;

import com.drcnet.highway.service.dataclean.DataCleanService;
import com.drcnet.highway.service.TietouExtractionService;
import com.drcnet.highway.service.TietouScoreGyhService;
import com.drcnet.highway.util.Result;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/8 15:11
 * @Description:
 */
@RestController
@RequestMapping("dataClean")
@Api("数据清洗接口")
public class DataCleanController {

    @Resource
    private DataCleanService dataCleanService;
    @Resource
    private TietouExtractionService tietouExtractionService;
    @Resource
    private TietouScoreGyhService tietouScoreGyhService;

    @GetMapping("dataClean")
    public Result dataClean(){
        dataCleanService.featureClean();
        return Result.ok();
    }



}
