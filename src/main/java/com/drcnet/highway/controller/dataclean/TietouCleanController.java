package com.drcnet.highway.controller.dataclean;

import com.drcnet.highway.entity.TietouFeatureStatistic;
import com.drcnet.highway.service.TietouSameStationFrequentlyService;
import com.drcnet.highway.service.TietouStatisticsService;
import com.drcnet.highway.service.dataclean.TietouCleanService;
import com.drcnet.highway.util.Result;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * @Author: penghao
 * @CreateTime: 2019/4/2 13:56
 * @Description:
 */
@RestController
@RequestMapping("tietouClean")
public class TietouCleanController {

    @Resource
    private TietouCleanService tietouCleanService;

    @Resource
    private TietouSameStationFrequentlyService tietouSameStationFrequentlyService;

    @Resource
    private TietouStatisticsService tietouStatisticsService;

    @ApiOperation("更新原始记录表的各种ID：envlp_id,vlp_id,rk_id,ck_id")
    @GetMapping("updateForeignIds")
    public Result updateForeignIds() {
        tietouCleanService.updateForeignId();
        return Result.ok();
    }

    @ApiOperation("将车牌字典表信息存到redis")
    @GetMapping("initCarDic2Cache")
    public Result initCarDic2Cache() {
        tietouCleanService.initCarDic2Cache();
        return Result.ok();
    }

    @ApiOperation("将use_flag为false的字典表信息存到redis")
    @GetMapping("initUseFlagFalseCarDic2Cache")
    public Result initUseFlagFalseCarDic2Cache(Integer start) {
        if (start == null) {
            start = 1;
        }
        tietouCleanService.initUseFlagFalseCarDic2Cache(start);
        return Result.ok();
    }

    @ApiOperation("统计数据的站点间平均速度")
    @GetMapping("statisticsStationAvgSpeed")
    public Result statisticsStationAvgSpeed(Integer start) {
        if (start == null) {
            start = 1;
        }
        tietouCleanService.statisticsStationAvgSpeed(start);
        return Result.ok();
    }

    @ApiOperation("将车轴数和车型统计到车辆字典表中")
    @GetMapping("statisticsAlexNumAndCarType")
    public Result statisticsAlexNumAndCarType(Integer start) {
        if (start == null) {
            start = 0;
        }
        tietouCleanService.statisticsAlexNumAndCarType(start);
        return Result.ok();
    }

    @ApiOperation("统计最大载重和最小载重到车辆字典表中")
    @GetMapping("updateMaxAndMinWeight")
    public Result updateMaxAndMinWeight(Integer start){
        if (start == null) {
            start = 1;
        }
        tietouCleanService.updateMaxAndMinWeight(start);
        return Result.ok();
    }

    @ApiOperation("将10项特征不包含先出后进和时间重叠的记录新增到extraction表中")
    @GetMapping("insertExtractionData")
    public Result insertExtractionData(Integer start) {
        if (start == null) {
            start = 0;
        }
        tietouCleanService.insertExtractionData(start);
        return Result.ok();
    }

    @ApiOperation("统计重复驶入风险，标记重复驶入标记和分组标记")
    @GetMapping("statisticSameTimeRange")
    public Result statisticSameTimeRange(Integer startMonth, Integer endMonth){
        if (startMonth == null) {
            startMonth = 201901;
        }
        if (endMonth == null) {
            endMonth = 201905;
        }
        for (int i = startMonth; i <= endMonth; i++) {
            tietouCleanService.statisticSameTimeRange(i);
        }
        return Result.ok();
    }

    @ApiOperation("统计先出后进,并标记到extraction表内")
    @GetMapping("outAndInByJava")
    public Result outAndInByJava(Integer startMonth, Integer endMonth){
        if (startMonth == null) {
            startMonth = 201901;
        }
        if (endMonth == null) {
            endMonth = 201905;
        }
        for (int i = startMonth; i <= endMonth; i++) {
            tietouCleanService.calcOutAndInByJava(i);
        }
        //设置额外的数据
        setInOutFrequencyExt();
        /*for (int i = 201901; i <= 201905; i++) {
            tietouCleanService.calcOutAndInByJava(i);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }*/
        return Result.ok();
    }

    /**
     * 在将每项异常的数量统计完写入static表后，在算法跑分之前需要将is_free_car进行赋值
     * 方便排除免费的内部车辆
     * @return
     */
    @ApiOperation("修改static表内is_free_car的值")
    @GetMapping("updateIsFreeCar")
    public Result updateIsFreeCar(Integer start){
        if (start == null) {
            start = 1;
        }
        tietouCleanService.updateIsFreeCar(start);
        return Result.ok();
    }

    @ApiOperation("统计每个车牌12项特征的违规次数-1、按月分别统计")
    @GetMapping("statisticFeatureAmountByMonth")
    public Result statisticFeatureAmountByMonth() {
        for (int i = 201907; i <= 201907; i++) {
            tietouCleanService.statisticFeatureAmountByMonth(i);
        }
        return Result.ok();
    }

    @ApiOperation("统计每个车牌12项特征的违规次数-2、将所有月份的违规数量按车牌统计到一起")
    @GetMapping("statisticsAllAmountFrom")
    public Result statisticsAllAmountFrom(){
        Map<Integer, TietouFeatureStatistic> dataMap = new ConcurrentHashMap<>(2<<24);
        /*for (int i = 201801; i <= 201812; i++) {
            tietouCleanService.statisticsAllAmountFrom(i,dataMap);
        }*/
        for (int i = 201901; i <= 201907; i++) {
            tietouCleanService.statisticsAllAmountFrom(i,dataMap);
        }
        tietouCleanService.insertAmountStatistics2DB(dataMap);
        return Result.ok();
    }

    @ApiOperation("修正标志")
    @GetMapping("amendStationFlag")
    public Result amendStationFlag() {

        tietouCleanService.amendStationFlag();
        return Result.ok();
    }

    @ApiOperation("在归一化后的得分表内给从二绕经过的车辆做标记")
    @GetMapping("mark2ndRoundCars")
    public Result mark2ndRoundCars(){
        tietouCleanService.mark2ndRoundCars();
        return Result.ok();
    }


    @ApiOperation("修正车牌不一致的记录")
    @GetMapping("replaceSameCarNum")
    public Result replaceSameCarNum(){
        CountDownLatch latch = new CountDownLatch(5);
        for (int i = 201901; i <= 201905; i++) {
            tietouCleanService.replaceSameCarNum(i,latch);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        return Result.ok();
    }

    @ApiOperation("更新statistics表内的carType")
    @GetMapping("updateCarType")
    public Result updateCarType(){
        tietouCleanService.updateCarType();
        return Result.ok();
    }

    @ApiOperation("更新statistics表内的通行总次数")
    @GetMapping("updateTransitTimes")
    public Result updateTransitTimes(){
        tietouCleanService.updateTransitTimes();
        return Result.ok();
    }

    @ApiOperation("查询车牌不一致里入站车牌重复出现两次及以上的车牌")
    @GetMapping("listSameEnvlpMoreThan2")
    public Result listSameEnvlpMoreThan2(){
        tietouStatisticsService.listSameEnvlpMoreThan2();
        return Result.ok();
    }

    @ApiOperation("将车牌不一致较多的车牌打印为excel")
    @GetMapping("sameCarNum2Excel")
    public Result sameCarNum2Excel(){
        tietouStatisticsService.sameCarNum2Excel();
        return Result.ok();
    }

    @ApiOperation("将车牌不一致较多的车牌的违规次数详情打印为excel")
    @GetMapping("sameCarNumDetail2Excel")
    public Result sameCarNumDetail2Excel(){
        tietouStatisticsService.sameCarNumDetail2Excel();
        return Result.ok();
    }

    @ApiOperation("给长途轻载和短途重载打标记")
    @GetMapping("markOverweightAndLightweight")
    public Result markOverweightAndLightweight(){
        tietouCleanService.markOverweightAndLightweight();
        return Result.ok();
    }

    @ApiOperation("设置异常车牌的useFlag为false")
    @GetMapping("settingCarUseless")
    public Result settingCarUseless(){
        tietouCleanService.settingCarUseless();
        return Result.ok();
    }

    @ApiOperation("修正速度异常")
    @GetMapping("updateSpeedFlag")
    public Result updateSpeedFlag(){
        tietouCleanService.updateSpeedFlag();
        return Result.ok();
    }

    @ApiOperation("修正轴数异常")
    @GetMapping("updateAxlenumFlag")
    public Result updateAxlenumFlag(){
        tietouCleanService.updateAxlenumFlag();
        return Result.ok();
    }

    @ApiOperation("缓存免费的车辆")
    @GetMapping("cacheFreeCar")
    public Result cacheFreeCar(){
        tietouCleanService.cacheFreeCar();
        return Result.ok();
    }


    /**
     * 在算法跑完得分后，将score、cheating、violation、label的值copy到static表
     * 前端车辆异常详情时要用
     * @return
     */
    @ApiOperation("修改static表内的score、cheating、violation、label的值")
    @GetMapping("copyScore2Static")
    public Result copyScore2Static(){
        tietouCleanService.copyScore2Static();
        return Result.ok();
    }

    /**
     * 导入tietou原始数据到数据库
     * @return
     */
    @ApiOperation("导入tietou原始数据到数据库")
    @GetMapping("importTietou2DB")
    public Result importTietou2DB(String fileName, Integer count){
        tietouCleanService.importTietou2DB(fileName, count);
        return Result.ok();
    }



    @ApiOperation("设置先出后进表内的附加数据(通行距离，时间，载重等)")
    @PutMapping("setInOutFrequencyExt")
    public Result setInOutFrequencyExt(){
        tietouSameStationFrequentlyService.setInOutFrequencyExt();
        return Result.ok();
    }


}
