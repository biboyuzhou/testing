package com.drcnet.highway.controller.dataclean;

import com.drcnet.highway.service.TietouSameStationFrequentlyService;
import com.drcnet.highway.service.TietouService;
import com.drcnet.highway.service.TietouStatisticsService;
import com.drcnet.highway.service.cache.TietouCacheService;
import com.drcnet.highway.service.dataclean.TietouCleanService;
import com.drcnet.highway.service.dic.TietouCarDicService;
import com.drcnet.highway.util.Result;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;

/**
 * @Author: penghao
 * @CreateTime: 2019/4/2 13:56
 * @Description:
 */
@Slf4j
@RestController
@RequestMapping("tietouClean")
public class TietouCleanController {

    @Resource
    private TietouCleanService tietouCleanService;
    @Resource
    private TietouService tietouService;
    @Resource
    private TietouCarDicService carDicService;

    @Resource
    private TietouSameStationFrequentlyService tietouSameStationFrequentlyService;

    @Resource
    private TietouStatisticsService tietouStatisticsService;
    @Resource
    private TietouCacheService tietouCacheService;

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
    public Result statisticsStationAvgSpeed() {

        tietouCleanService.insertStationFeatureStatisticData();
        return Result.ok();
    }

    @ApiOperation("将车轴数和车型统计到车辆字典表中")
    @GetMapping("statisticsAlexNumAndCarType")
    public Result statisticsAlexNumAndCarType(Integer start) {
        if (start == null) {
            start = 0;
        }
        tietouCleanService.statisticsAlexNumAndCarType(start, null);
        return Result.ok();
    }

    @ApiOperation("统计最大载重和最小载重到车辆字典表中")
    @GetMapping("updateMaxAndMinWeight")
    public Result updateMaxAndMinWeight(Integer start) {
        if (start == null) {
            start = 1;
        }
        tietouCleanService.updateMaxAndMinWeight(start, null);
        return Result.ok();
    }

    @ApiOperation("统计最大载重和最小载重到车辆字典表中,批量统计")
    @GetMapping("updateMaxAndMinWeightAsync")
    public Result updateMaxAndMinWeightAsync() {
        Integer maxId = carDicService.selectMaxId();
        int dis = 1000;
        int beginIndex = 0;
        int amount = maxId - beginIndex;
        int latchSize = amount % dis == 0 ? amount / dis : amount / dis + 1;
        CountDownLatch latch = new CountDownLatch(latchSize);
        for (int i = beginIndex; i <= maxId; i += dis) {
            int end = i + dis < maxId ? i + dis : maxId;
            tietouCleanService.massUpdateMaxAndMinWeight(i + 1, end, latch);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("{}", e);
            Thread.currentThread().interrupt();
        }
        return Result.ok();
    }


    @ApiOperation("将10项特征不包含先出后进和时间重叠的记录新增到extraction表中")
    @GetMapping("insertExtractionData")
    public Result insertExtractionData(Integer start, Integer end) {
        if (start == null) {
            start = 0;
        }
        tietouCleanService.insertExtractionData(start, end);
        log.info("10项特征统计已完成，{} - {} 区间的数据已新增至数据库", start, end);
        return Result.ok();
    }

    @ApiOperation("统计重复驶入风险，标记重复驶入标记和分组标记")
    @GetMapping("statisticSameTimeRange")
    public Result statisticSameTimeRange(Integer startMonth, Integer endMonth) {
        if (startMonth == null) {
            startMonth = 201901;
        }
        if (endMonth == null) {
            endMonth = 201908;
        }
        for (int i = startMonth; i <= endMonth; i++) {
            tietouCleanService.statisticSameTimeRange(i);
        }
        return Result.ok();
    }

    @ApiOperation("统计先出后进,并标记到extraction表内")
    @GetMapping("outAndInByJava")
    public Result outAndInByJava(Integer startMonth, Integer endMonth) {
        if (startMonth == null) {
            startMonth = 201901;
        }
        if (endMonth == null) {
            endMonth = 201908;
        }
        for (int i = startMonth; i <= endMonth; i++) {
            tietouCleanService.calcOutAndInByJava(i);
        }
        return Result.ok();
    }

    /**
     * 在将每项异常的数量统计完写入static表后，在算法跑分之前需要将is_free_car进行赋值
     * 方便排除免费的内部车辆
     *
     * @return
     */
    @ApiOperation("修改static表内is_free_car的值")
    @GetMapping("updateIsFreeCar")
    public Result updateIsFreeCar(Integer start) {
        if (start == null) {
            start = 1;
        }
        tietouCleanService.updateIsFreeCar(start);
        return Result.ok();
    }

    @ApiOperation("统计每个车牌每个月的违规次数，并合并到statistcs表中")
    @GetMapping("statisticsAllAmountFrom")
    public Result statisticsAllAmountFrom(Integer startMonth, Integer endMonth) {
        long timeMillis = System.currentTimeMillis();
        if (startMonth == null) {
            startMonth = 201901;
        }
        if (endMonth == null) {
            endMonth = 201908;
        }
        //先删减表
        tietouCleanService.truncateStatisticMonthTable();
        for (int i = startMonth; i <= endMonth; i++) {
            tietouCleanService.insertStatisticsMonthData(i);
        }
        tietouCleanService.truncateStatisticTable();
        tietouCleanService.insertStatisticsByMonth();
        log.info("tietou_feature_statistics表统计数据共耗时:{} 秒", (System.currentTimeMillis() - timeMillis) / 1000);
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
    public Result mark2ndRoundCars() {
        tietouCleanService.mark2ndRoundCars();
        return Result.ok();
    }


    @ApiOperation("修正车牌不一致的记录")
    @GetMapping("replaceSameCarNum")
    public Result replaceSameCarNum() {
        CountDownLatch latch = new CountDownLatch(5);
        for (int i = 201901; i <= 201905; i++) {
            tietouCleanService.replaceSameCarNum(i, latch);
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
    public Result updateCarType() {
        tietouCleanService.updateCarType();
        return Result.ok();
    }

    @ApiOperation("更新statistics表内的通行总次数")
    @GetMapping("updateTransitTimes")
    public Result updateTransitTimes() {
        tietouCleanService.updateTransitTimes();
        return Result.ok();
    }

    @ApiOperation("查询车牌不一致里入站车牌重复出现两次及以上的车牌")
    @GetMapping("listSameEnvlpMoreThan2")
    public Result listSameEnvlpMoreThan2() {
        tietouStatisticsService.listSameEnvlpMoreThan2();
        return Result.ok();
    }

    @ApiOperation("将车牌不一致较多的车牌打印为excel")
    @GetMapping("sameCarNum2Excel")
    public Result sameCarNum2Excel() {
        tietouStatisticsService.sameCarNum2Excel();
        return Result.ok();
    }

    @ApiOperation("将车牌不一致较多的车牌的违规次数详情打印为excel")
    @GetMapping("sameCarNumDetail2Excel")
    public Result sameCarNumDetail2Excel() {
        tietouStatisticsService.sameCarNumDetail2Excel();
        return Result.ok();
    }

    @ApiOperation("给长途轻载和短途重载打标记")
    @GetMapping("markOverweightAndLightweight")
    public Result markOverweightAndLightweight() {
        tietouCleanService.markOverweightAndLightweight();
        return Result.ok();
    }

    @ApiOperation("设置异常车牌的useFlag为false")
    @GetMapping("settingCarUseless")
    public Result settingCarUseless() {
        tietouCleanService.settingCarUseless();
        return Result.ok();
    }

    @ApiOperation("修正速度异常")
    @GetMapping("updateSpeedFlag")
    public Result updateSpeedFlag() {
        tietouCleanService.updateSpeedFlag();
        return Result.ok();
    }

    @ApiOperation("修正轴数异常")
    @GetMapping("updateAxlenumFlag")
    public Result updateAxlenumFlag() {
        tietouCleanService.updateAxlenumFlag();
        return Result.ok();
    }

    @ApiOperation("缓存免费的车辆")
    @GetMapping("cacheFreeCar")
    public Result cacheFreeCar() {
        tietouCleanService.cacheFreeCar();
        return Result.ok();
    }


    /**
     * 在算法跑完得分后，将score、cheating、violation、label的值copy到static表
     * 前端车辆异常详情时要用
     *
     * @return
     */
    @ApiOperation("修改static表内的score、cheating、violation、label的值")
    @GetMapping("copyScore2Static")
    public Result copyScore2Static() {
        tietouCleanService.copyScore2Static();
        return Result.ok();
    }

    /**
     * 导入tietou原始数据到数据库
     *
     * @return
     */
    @ApiOperation("导入tietou原始数据到数据库")
    @GetMapping("importTietou2DB")
    public Result importTietou2DB(String fileName, Integer count) {
        tietouCleanService.importTietou2DB(fileName, count);
        return Result.ok();
    }


    @ApiOperation("设置先出后进表内的附加数据(通行距离，时间，载重等)")
    @PutMapping("setInOutFrequencyExt")
    public Result setInOutFrequencyExt() {
        tietouSameStationFrequentlyService.setInOutFrequencyExt();
        return Result.ok();
    }

    @ApiOperation("将tietou_2019内的指定高速公路的数据筛选出来到tietou")
    @GetMapping("filterSpecifiedData2tietou")
    public Result filterSpecifiedData2tietou() {
        tietouCleanService.filterSpecifiedData2tietou();
        return Result.ok();
    }

    @ApiOperation(value = "将通行记录加入缓存", notes = "将entime,extime,rkId,ckId,envlpId,vlpId组合成一个字符串存入缓存")
    @GetMapping("setOriginalCache")
    public Result setOriginalCache() {
        long timeMillis = System.currentTimeMillis();
        log.info("开始将数据缓存至redis");
        LocalDate now = LocalDate.now();
        LocalDate localDate = tietouService.selectMaxTime(now);
        LocalDate maxDate = localDate.plusDays(1);
        for (int i = 0; i < 3; i++) {
            LocalDate minusDate = maxDate.minusMonths(1);
            tietouCacheService.initCache(minusDate, maxDate);
            maxDate = minusDate;
        }
        log.info("数据缓存成功，耗时:{}秒", (System.currentTimeMillis() - timeMillis) / 1000);
        return Result.ok();
    }

    @ApiOperation(value = "开启所有统计步骤")
    @PostMapping("beginAllStatisticsSteps")
    public Result beginAllStatisticsSteps(Integer startMonth, Integer endMonth, String secret) {
        if (!"!qaz@wsx".equals(secret)) {
            return Result.error("密钥错误");
        }
        long firstTimeMillis = System.currentTimeMillis();
        //统计站点间的平均速度
        tietouCleanService.truncateStationStatisticTable();
        tietouCleanService.insertStationFeatureStatisticData();
        firstTimeMillis = printStep(firstTimeMillis, 1);
        //统计10项特征
        tietouCleanService.truncateExtractionTable();
        Result result1 = insertExtractionData(null, null);
        if (result1.getCode() != 200) {
            return Result.error("步骤2执行失败");
        } else {
            firstTimeMillis = printStep(firstTimeMillis, 2);
        }
        //时间重叠
        Result result2 = statisticSameTimeRange(startMonth, endMonth);
        if (result2.getCode() != 200) {
            return Result.error("步骤3执行失败");
        } else {
            firstTimeMillis = printStep(firstTimeMillis, 3);
        }
        //先出后进
        tietouSameStationFrequentlyService.truncate();
        Result result3 = outAndInByJava(startMonth, endMonth);
        if (result3.getCode() != 200) {
            return Result.error("步骤4执行失败");
        } else {
            firstTimeMillis = printStep(firstTimeMillis, 4);
        }
        //统计车辆违规次数至statistics表
        Result result4 = statisticsAllAmountFrom(startMonth, endMonth);
        if (result4.getCode() != 200) {
            return Result.error("步骤5执行失败");
        } else {
            firstTimeMillis = printStep(firstTimeMillis, 5);
        }
        //更新免费车牌
        Result result5 = updateIsFreeCar(null);
        if (result5.getCode() != 200) {
            return Result.error("步骤6执行失败");
        } else {
            printStep(firstTimeMillis, 6);
        }
        return Result.ok();
    }

    private long printStep(long firstTimeMillis, int step) {
        long currentTime = System.currentTimeMillis();
        log.info("步骤" + step + "执行完成，耗时:{} 秒", (currentTime - firstTimeMillis) / 1000);
        return currentTime;
    }



    @ApiOperation(value = "将通行记录写入es")
    @GetMapping("pushTietou2Es")
    public Result pushTietou2Es() {
        tietouCleanService.pushTietou2Es();
        return Result.ok();
    }

    @ApiOperation(value = "将通行记录写入es")
    @GetMapping("pushTietou2EsById")
    public Result pushTietou2EsById() {
        tietouCleanService.pushTietou2EsById();
        return Result.ok();
    }
}
