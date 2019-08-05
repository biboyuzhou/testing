package com.drcnet.highway.controller;

import com.drcnet.highway.constants.TipsConsts;
import com.drcnet.highway.dto.PeriodAmountDto;
import com.drcnet.highway.dto.RiskAmountDto;
import com.drcnet.highway.dto.RiskPeriodAmount;
import com.drcnet.highway.dto.request.CheatingListTimeSearchDto;
import com.drcnet.highway.dto.request.TravelRecordQueryDto;
import com.drcnet.highway.dto.request.CheatingListDto;
import com.drcnet.highway.dto.response.*;
import com.drcnet.highway.entity.TietouFeatureStatisticGyh;
import com.drcnet.highway.entity.dic.TietouCarDic;
import com.drcnet.highway.exception.MyException;
import com.drcnet.highway.service.TietouExtractionService;
import com.drcnet.highway.service.TietouScoreGyhService;
import com.drcnet.highway.service.TietouService;
import com.drcnet.highway.service.TietouStationDicService;
import com.drcnet.highway.service.dataclean.TietouCleanService;
import com.drcnet.highway.service.dic.TietouCarDicService;
import com.drcnet.highway.util.DownloadUtil;
import com.drcnet.highway.util.EntityUtil;
import com.drcnet.highway.util.Result;
import com.drcnet.highway.util.validate.PageValid;
import com.drcnet.highway.util.validate.QueryValid;
import com.drcnet.highway.vo.PageVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/8 10:45
 * @Description:
 */
@Slf4j
@RestController
@RequestMapping("mainPage")
@Api(value = "MainPageController",tags = "二期首页接口")
public class MainPageController {

    @Resource
    private TietouCarDicService tietouCarDicService;
    @Resource
    private TietouService tietouService;
    @Resource
    private TietouScoreGyhService tietouScoreGyhService;
    @Resource
    private TietouExtractionService tietouExtractionService;
    @Resource
    private TietouCleanService tietouCleanService;
    @Resource
    private TietouStationDicService tietouStationDicService;


    @GetMapping("queryCarNo")
    @ApiOperation("查询车牌号")
    public Result queryCarNo(@RequestParam String carNo, @RequestParam Integer carType){
        List<TietouCarDic> carDicList = tietouCarDicService.queryCarNo(carNo, carType);
        return Result.ok(carDicList);
    }

    @GetMapping("listCheatingCar")
    @ApiOperation(value = "查询违规列表",notes = "carType:-1查询所有，0货车，1客车")
    public Result listCheatingCar(@Validated({QueryValid.class, PageValid.class}) CheatingListDto dto){
        if (dto.getPageSize()>300) {
            return Result.error("数据量太大");
        }
        PageVo<TietouFeatureStatisticGyh> pageVo = tietouScoreGyhService.listCheatingCar(dto);
        return Result.ok(pageVo);
    }

    @GetMapping("listCheatingCarByTime")
    @ApiOperation(value = "查询违规列表",notes = "carType:-1查询所有，0货车，1客车; 开始日期和结束日期筛选")
    public Result listCheatingCarByTime(@Validated({QueryValid.class, PageValid.class}) CheatingListTimeSearchDto dto){
        if (dto.getPageSize() > 300) {
            return Result.error("数据量太大");
        }
        PageVo<TietouFeatureStatisticGyh> pageVo = tietouScoreGyhService.listCheatingCarByTime(dto);
        return Result.ok(pageVo);
    }

    @GetMapping("exportCheatingCar")
    @ApiOperation("导出违规列表到Excel")
    public ResponseEntity<byte[]> exportCheatingCar(@Validated({QueryValid.class}) CheatingListDto dto){
        String fileName = "车辆逃费风险名单"+dto.getBeginMonth()+".xlsx";
        byte[] bytes = tietouScoreGyhService.exportCheatingCar(dto,"车辆逃费风险名单"+dto.getBeginMonth()+".xlsx");
        return DownloadUtil.download(bytes,fileName);
    }


    @GetMapping("getCheatingCount")
    @ApiOperation("查询某个月份违规数量")
    public Result getCheatingCount(Integer beginMonth,Integer carType){
        if (carType == null){
            carType = -1;
        }
        RiskAmountDto cheatingCount = tietouService.getCheatingCount(beginMonth,carType);
        return Result.ok(cheatingCount);
    }

    @GetMapping("listCheatingPeriod")
    @ApiOperation("查询某个月每天的违规数量")
    public Result listCheatingPeriod(@RequestParam String beginMonth,Integer carType){
        List<PeriodAmountDto> periodAmountDtos = tietouService.listCheatingPeriod(beginMonth,carType);
        return Result.ok(periodAmountDtos);
    }

    @GetMapping("getRiskProportion")
    @ApiOperation("获得风险类型占比数量")
    public Result getRiskProportion(@RequestParam Integer beginMonth,Integer carType){
        RiskPeriodAmount riskProportion = tietouScoreGyhService.getRiskProportion(beginMonth,carType);
        return Result.ok(riskProportion);
    }

    @ApiOperation("查询通行记录")
    @PostMapping("queryTravelRecords")
    public Result queryTravelRecords(@RequestBody TravelRecordQueryDto travelRecordQueryDto){
        if (StringUtils.isEmpty(travelRecordQueryDto.getInCarNo()) && StringUtils.isEmpty(travelRecordQueryDto.getOutCarNo())
                && StringUtils.isEmpty(travelRecordQueryDto.getCard())) {
            throw new MyException("入口车牌、出口车牌、卡号不能都为空！");
        }

        if (!StringUtils.isEmpty(travelRecordQueryDto.getInDate())) {
            EntityUtil.dateChecked(travelRecordQueryDto.getInDate());
        }

        if (!StringUtils.isEmpty(travelRecordQueryDto.getOutDate())) {
            EntityUtil.dateChecked(travelRecordQueryDto.getOutDate());
        }

        if (travelRecordQueryDto.getPageNum() == null) {
            travelRecordQueryDto.setPageNum(1);
        }
        if (travelRecordQueryDto.getPageSize() == null) {
            travelRecordQueryDto.setPageSize(10);
        }
        try {
            PageVo pageVo = tietouService.queryTravelRecords(travelRecordQueryDto);
            return Result.ok(pageVo);
        } catch (ParseException e) {
            throw new MyException("日期格式转化失败！");
        }
    }

    @GetMapping("getSameTimeRangeStatic")
    @ApiOperation("统计指定车辆的时间重叠次数")
    public Result getSameTimeRangeStatic(@RequestParam Integer carId){
        SameTimeRangeStaticDto sameTimeRangeStaticDto = tietouExtractionService.getSameTimeRangeStatic(carId);
        return Result.ok(sameTimeRangeStaticDto);
    }

    @GetMapping("getDiffCarNoStatic")
    @ApiOperation("统计指定车辆的进出车牌不一致数据")
    public Result getDiffCarNoStatic(@RequestParam Integer carId){
        DiffCarNoStaticDto diffCarNoStaticDto = tietouService.getDiffCarNoStatic(carId);
        return Result.ok(diffCarNoStaticDto);
    }

    @GetMapping("queryDiffCarNoInOutDataByPage")
    @ApiOperation("根据进口和出口车牌分页查询进出口车牌不一致数据")
    public Result queryDiffCarNoInOutDataByPage(@RequestParam Integer envlpId, @RequestParam Integer vlpId, @RequestParam Integer pageSize, @RequestParam Integer pageNum){
        if (pageNum == null) {
            pageNum = 1;
        }
        if (pageSize == null) {
            pageSize = 10;
        }
        PageVo pageVo = tietouService.queryDiffCarNoInOutData(envlpId, vlpId, pageNum, pageSize);
        return Result.ok(pageVo);
    }

    @GetMapping("queryCompositeRisk")
    @ApiOperation("根据车牌查询综合风险")
    public Result queryCompositeRisk(@RequestParam Integer carId){
        if (carId == null) {
            throw new MyException("车辆id必传！");
        }
        CompositeRiskDto compositeRiskDto = tietouService.queryCompositeRisk(carId);
        return Result.ok(compositeRiskDto);
    }

    @PostMapping("uploadSqlFile")
    @ApiOperation("上传数据库文件")
    public Result uploadSqlFile(MultipartFile file){
        long timeMillis = System.currentTimeMillis();
        if (file == null || file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        long size = file.getSize();
        File fileLocation = new File("/project/sql/" + file.getOriginalFilename());
        try {
            file.transferTo(fileLocation);
            log.info("文件大小:{},上传耗时:{}毫秒", size, System.currentTimeMillis() - timeMillis);
            return Result.ok();
        } catch (IOException e) {
            log.error("{}", e);
            return Result.error(TipsConsts.SERVER_ERROR);
        }

    }

    /**
     * 统计二绕互相之间通行的次数
     * 新版首页展示需要
     * @return
     */
    @ApiOperation("统计二绕互相之间通行的次数")
    @GetMapping("statistic2ndCount")
    public Result statistic2ndCount(){
        List<StationTripCountDto> tripCountList = tietouCleanService.statistic2ndCount();
        return Result.ok(tripCountList);
    }

    /**
     * 统计所有通行记录里每个车型的数量
     * 新版首页展示需要
     * @return
     */
    @ApiOperation("统计所有通行记录里每个车型的数量")
    @GetMapping("statisticCarTypeCount")
    public Result statisticCarTypeCount(){
        List<CommonTypeCountDto> commonTypeCountDtoList = tietouService.statisticCarTypeCount();
        return Result.ok(commonTypeCountDtoList);
    }

    /**
     * 统计二绕每个站点出的车辆总数、高中低风险数
     * 新版首页展示需要
     * @return
     */
    @ApiOperation("统计二绕每个站点出的车辆总数、高中低风险数")
    @GetMapping("statistic2ndStationRiskCount")
    public Result statistic2ndStationRiskCount(){
        List<StationRiskCountDto> riskCountDtoList = tietouService.statistic2ndStationRiskCount();
        return Result.ok(riskCountDtoList);
    }

    /**
     * 统计二绕每个站点出的车辆总数、高中低风险数
     * 新版首页展示需要
     * @return
     */
    @ApiOperation("二绕站点")
    @GetMapping("get2ndRoundStation")
    public Result get2ndRoundStation(){
        List<StationDicDto> stationDicDtoList = tietouStationDicService.get2ndRoundStation();
        return Result.ok(stationDicDtoList);
    }

}
