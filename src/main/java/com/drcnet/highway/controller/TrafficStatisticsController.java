package com.drcnet.highway.controller;

import com.drcnet.highway.constants.ModuleConsts;
import com.drcnet.highway.dto.request.DateSearchDto;
import com.drcnet.highway.service.TrafficStatisticsService;
import com.drcnet.highway.util.Result;
import com.drcnet.usermodule.annotation.PermissionCheck;
import com.drcnet.usermodule.consts.AuthConsts;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/14 17:45
 * @Description:
 */
@RestController
@RequestMapping("trafficStatistics")
@Api(tags = "通行次数统计接口")
@Slf4j
public class TrafficStatisticsController {

    @Resource
    private TrafficStatisticsService trafficStatisticsService;

    @PutMapping("statisticsAllTraffic")
    @ApiOperation("统计所有通行次数数据至数据库")
    @PermissionCheck(AuthConsts.ALL)
    public Result statisticsAllTraffic(){
        trafficStatisticsService.statisticsAllTraffic();
        return Result.ok();
    }

    @GetMapping("listStationTrafficStatistics")
    @ApiOperation(value = "获得站点通行次数统计",notes = "type:0入口数据，1出口数据;flag:0日期数据，1站点数据")
    @PermissionCheck(ModuleConsts.DATA_SURVEY)
    public Result listStationTrafficStatistics(DateSearchDto dateSearchDto, @RequestParam Integer type, @RequestParam Integer flag){
        return Result.ok(trafficStatisticsService.listStationTrafficStatistics(dateSearchDto,type,flag));
    }

    /*@GetMapping("trafficStatisticsTest")
    public Result trafficStatisticsTest(){
        trafficStatisticsService.incrementDataStatistics(null,null, trafficStatisticsService);
        return Result.ok();
    }*/

}
