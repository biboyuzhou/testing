package com.drcnet.highway.controller.dataclean;

import com.drcnet.highway.util.Result;
import com.drcnet.highway.service.dataclean.CompareService;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author jack
 * @Date: 2019/6/3 11:05
 * @Desc: 标志站比对
 **/
@RestController
@RequestMapping("compare")
public class CompareController {
    @Resource
    private CompareService compareService;

    @ApiOperation("将tietou表的理论标志站和实际标志站存入redis")
    @GetMapping("cacheStationFlagInfo")
    public Result cacheStationFlagInfo(){
        compareService.cacheStationFlagInfo();
        return Result.ok();
    }

    @ApiOperation("将算法跑出结果最高分2000车辆与二绕取交集，并缓存结果")
    @GetMapping("cacheSecondHighAlarmCar")
    public Result cacheSecondHighAlarmCar(){
        compareService.cacheSecondHighAlarmCar();
        return Result.ok();
    }

    @ApiOperation("获取缓存的标志站的数量")
    @GetMapping("getCacheStationFlagInfoSize")
    public Result getCacheStationFlagInfoSize(){
        compareService.getCacheStationFlagInfoSize();
        return Result.ok();
    }

    @ApiOperation("对缓存的标志站的数据进行排序")
    @GetMapping("sortStationFlagInfo")
    public Result sortStationFlagInfo(){
        compareService.sortStationFlagInfo();
        return Result.ok();
    }

    @ApiOperation("对缓存的标志站的数据进行排序")
    @GetMapping("chooseUnnecessaryId")
    public Result chooseUnnecessaryId(){
        compareService.chooseUnnecessaryId();
        return Result.ok();
    }

    @ApiOperation("缓存高风险的车牌数据")
    @GetMapping("cacheHighAlarmCarno")
    public Result cacheHighAlarmCarno(){
        compareService.cacheHighAlarmCarno();
        return Result.ok();
    }

    @ApiOperation("将original的数据转化到铁投表中")
    @GetMapping("convertOriginal2Tietou")
    public Result convertOriginal2Tietou(){
        compareService.convertOriginal2Tietou();
        return Result.ok();
    }

    @ApiOperation("将2019年新的车辆数据添加到缓存中")
    @GetMapping("addNewCarId2Cache")
    public Result addNewCarId2Cache(){
        compareService.addNewCarId2Cache();
        return Result.ok();
    }

    @ApiOperation("更新车牌id")
    @GetMapping("updateCarNoId")
    public Result updateCarNoId(){
        compareService.updateCarNoId();
        return Result.ok();
    }

    @ApiOperation("批量插入铁头表数据id")
    @GetMapping("insertTietouByBatch")
    public Result insertTietouByBatch(){
        compareService.insertTietouByBatch();
        return Result.ok();
    }

    @ApiOperation("修改异常车牌use_flag")
    @GetMapping("updateAbnormalCarDic")
    public Result updateAbnormalCarDic(){
        compareService.updateAbnormalCarDic();
        return Result.ok();
    }

    @ApiOperation("修改异常车牌use_flag")
    @GetMapping("updateZhouShuDiff")
    public Result updateZhouShuDiff(){
        compareService.updateZhouShuDiff();
        return Result.ok();
    }

    @ApiOperation("将轴数异常数据增加占比大于20%判断， 服务器执行大约耗时750s")
    @GetMapping("getZhouShuDiffInfo")
    public Result getZhouShuDiffInfo(){
        compareService.getZhouShuDiffInfo();
        return Result.ok();
    }


    @ApiOperation("对每月的统计数据进行汇总，得到每个车牌总的异常次数，最终结果为写入tietou_feature_statistic")
    @GetMapping("cacheStaticData")
    public Result cacheStaticData(){
        compareService.cacheStaticData();
        return Result.ok();
    }


    @ApiOperation("缓存铁头表数据")
    @GetMapping("cacheTietou")
    public Result cacheTietou(){
        int step = 1000000;
        // 21611570
        int maxId = 21611570;
        for (int i = 0; i < maxId; i += step) {
            compareService.cacheTietou(i);
            compareService.getCacheTietou(i);
        }

        return Result.ok();
    }

    @ApiOperation("从缓存取出铁头表数据并写入数据库")
    @GetMapping("getCacheTietou")
    public Result getCacheTietou(){
        compareService.getCacheTietou(21000000);
        return Result.ok();
    }

    @ApiOperation("筛选出出口车型与默认车型不一致的车牌")
    @GetMapping("getSameNumCar")
    public Result getSameNumCar(){
        compareService.getSameNumCar();
        return Result.ok();
    }

    @ApiOperation("将出口车型与默认车型不一致的车牌写入数据库")
    @GetMapping("processSameNumCar")
    public Result processSameNumCar(){
        compareService.processSameNumCar();
        return Result.ok();
    }

    @ApiOperation("将铁头表数据修改为新的vlpId")
    @GetMapping("updateTietouVlpId")
    public Result updateTietouVlpId(){
        compareService.updateTietouVlpId();
        return Result.ok();
    }

    @ApiOperation("检查tietou表vlpId是否为car_dic_all里的id，如果不是则修改")
    @GetMapping("checkAndUpdateTietouVlpId")
    public Result checkAndUpdateTietouVlpId(){
        compareService.checkAndUpdateTietouVlpId();
        return Result.ok();
    }

}