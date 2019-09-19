package com.drcnet.highway.controller.dataclean;

import com.drcnet.highway.service.TietouExtractionService;
import com.drcnet.highway.service.TietouScoreGyhService;
import com.drcnet.highway.service.dataclean.DataCleanService;
import com.drcnet.highway.service.dataclean.TietouCleanService;
import com.drcnet.highway.util.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    @Resource
    private TietouCleanService tietouCleanService;

    @GetMapping("dataClean")
    public Result dataClean() {
        dataCleanService.featureClean();
        return Result.ok();
    }

    /**
     * 删除缓存
     *
     * @return
     */
    @ApiOperation("删除缓存")
    @GetMapping("deleteCache")
    public Result deleteCache(String key) {
        dataCleanService.deleteCache(key);
        return Result.ok();
    }

    /**
     * 删除并重构首页缓存
     *
     * @return
     */
    @ApiOperation("删除并重构首页缓存")
    @GetMapping("deleteAndRebuildFirstPageCache")
    public Result deleteAndRebuildFirstPageCache() {
        dataCleanService.deleteFirstPageCache();
        dataCleanService.rebuildCache();
        return Result.ok();
    }

    @ApiOperation("查询出数据库内正常但车牌异常的记录")
    @GetMapping("listUselessCars")
    public Result listUselessCars() {
        //查询异常车牌
        dataCleanService.listUselessCarsWithUseFlagTrue();
        //更新至数据库
        dataCleanService.updateCarDicUseFlagByCache();
        //更新至缓存
//        tietouCleanService.initUseFlagFalseCarDic2Cache(1);
        //更新statistics表内的is_free_car字段
//        dataCleanService.updateStatisticsFreeCar();
        return Result.ok();
    }


    @ApiOperation("将car_dic表中useFlag为false的车牌在statistics表内设置is_free_car为1")
    @GetMapping("updateStatisticsFreeCar")
    public Result updateStatisticsFreeCar() {
        dataCleanService.updateStatisticsFreeCar();
        return Result.ok();
    }

    @ApiOperation("删除tietou表重复数据")
    @DeleteMapping("deleteRepeatData")
    public Result deleteRepeatData(){
        dataCleanService.deleteRepeatData(false);
        return Result.ok();
    }

}
