package com.drcnet.highway.controller;

import com.drcnet.highway.constants.enumtype.RiskFlagEnum;
import com.drcnet.highway.dao.TietouCarDicMapper;
import com.drcnet.highway.dto.PeriodAmountDto;
import com.drcnet.highway.dto.RiskMap;
import com.drcnet.highway.dto.ThroughFrequencyDto;
import com.drcnet.highway.dto.TurnoverStationDto;
import com.drcnet.highway.dto.request.CarMonthQueryDto;
import com.drcnet.highway.dto.request.RiskByRankRequest;
import com.drcnet.highway.dto.request.RiskInOutDto;
import com.drcnet.highway.dto.request.StationSpeedQueryDto;
import com.drcnet.highway.dto.response.CarDetailResponse;
import com.drcnet.highway.entity.TietouBlacklist;
import com.drcnet.highway.entity.TietouFeatureStatisticGyh;
import com.drcnet.highway.entity.dic.TietouCarDic;
import com.drcnet.highway.enums.BlackStatusEnum;
import com.drcnet.highway.enums.FeatureCodeEnum;
import com.drcnet.highway.service.*;
import com.drcnet.highway.util.DateUtils;
import com.drcnet.highway.util.EntityUtil;
import com.drcnet.highway.util.Result;
import com.drcnet.highway.util.validate.AddValid;
import com.drcnet.highway.util.validate.PageValid;
import com.drcnet.highway.util.validate.QueryValid;
import com.drcnet.highway.vo.CarTypeDistributionVo;
import com.drcnet.highway.vo.PageVo;
import com.drcnet.highway.vo.TurnoverVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/8 17:20
 * @Description:
 */
@Slf4j
@RestController
@RequestMapping("carDetail")
@Api(value = "CarDetailController", tags = "车辆详情接口")
public class CarDetailController {
    @Resource
    private TietouService tietouService;
    @Resource
    private TietouBlackListService tietouBlackListService;
    @Resource
    private TietouScoreGyhService tietouScoreGyhService;
    @Resource
    private TietouSameStationFrequentlyService tietouSameStationFrequentlyService;
    @Resource
    private StationFeatureStatisticService stationFeatureStatisticService;
    @Resource
    private TietouCarDicMapper tietouCarDicMapper;

    @ApiOperation(value = "查询车站进出数量详情",notes = "返回值中type：1白名单，2黑名单,0未标记")
    @GetMapping("listTurnovers")
    public Result listTurnovers(@RequestParam Integer carId, @RequestParam String beginMonth) {


        TurnoverVo turnoverVo = new TurnoverVo();
        //查询日区间
//        Future<List<PeriodAmountDto>> periodViolationAmount = tietouService.listPeriodViolationAmount(carId, beginMonth, null);
        Future<List<TurnoverStationDto>> listFuture = tietouService.listInAndOutStation(carId, beginMonth);
        //查询车牌
        TietouCarDic carInfo = tietouService.getCarInfoById(carId);
        if (carInfo == null){
            return Result.error("没有该车辆的数据");
        }
        //查询8大违规得分
        TietouFeatureStatisticGyh maxViolationScore = tietouService.getMaxViolationScore(carId);
        //查询通行次数
        Integer countThrough = tietouService.countThrough(carId, beginMonth);

        turnoverVo.setCarType(carInfo.getCarType());
        turnoverVo.setAxlenum(carInfo.getAxlenum());
        turnoverVo.setViolationScore(maxViolationScore);
        turnoverVo.setCarNo(carInfo.getCarNo());
        turnoverVo.setThroughAmount(countThrough);
        try {
            turnoverVo.setStationTurnovers(listFuture.get());
//            turnoverVo.setPeriodAmount(periodViolationAmount.get());
        } catch (InterruptedException| ExecutionException e) {
            log.error("{}",e);
            Thread.currentThread().interrupt();
        }

        if (carInfo.getWhiteFlag() != null && carInfo.getWhiteFlag()){
            turnoverVo.setType(BlackStatusEnum.WHITE.code);
        }else {
            TietouBlacklist blacklist = tietouBlackListService.queryByCarNoId(carId);
            if (blacklist != null) {
                turnoverVo.setType(BlackStatusEnum.BLACK.code);
                TietouFeatureStatisticGyh standardScore = new TietouFeatureStatisticGyh();
                String[] riskArray = blacklist.getRiskFlag().split(",");
                BigDecimal avg = blacklist.getScore().divide(new BigDecimal(riskArray.length), 2, BigDecimal.ROUND_HALF_UP);
                for (int i = 0; i < riskArray.length; i++) {
                    RiskFlagEnum riskFlagEnum = RiskFlagEnum.getEnumByRiskName(riskArray[i]);
                    if (riskFlagEnum == null) {
                        continue;
                    }
                    switch (riskFlagEnum) {
                        case LOW_SPEED:
                            standardScore.setLowSpeed(avg);
                            break;
                        case HIGH_SPEED:
                            standardScore.setHighSpeed(avg);
                            break;
                        case DIFF_FLAG_STATION_INFO:
                            standardScore.setDiffFlagstationInfo(avg);
                            break;
                        case SHORT_DIS_OVERWEIGHT:
                            standardScore.setShortDisOverweight(avg);
                            break;
                        case LONG_DIS_LIGHTWEIGHT:
                            standardScore.setLongDisLightweight(avg);
                            break;
                        case SAME_STATION:
                            standardScore.setSameStation(avg);
                            break;
                        case SAME_CAR_TYPE:
                            standardScore.setSameCarType(avg);
                            break;
                        case SAME_CAR_SITUATION:
                            standardScore.setSameCarSituation(avg);
                            break;
                        case DIFFERENT_ZHOU:
                            standardScore.setDifferentZhou(avg);
                            break;
                        case SAME_CAR_NUMBER:
                            standardScore.setSameCarNumber(avg);
                            break;
                        case MIN_OUT_IN:
                            standardScore.setMinOutIn(avg);
                            break;
                        case SAME_TIME_RANGE_AGAIN:
                            standardScore.setSameTimeRangeAgain(avg);
                            break;
                        case FLAG_STATION_LOST:
                            standardScore.setFlagstationLost(avg);
                            break;
                        default:
                            break;
                    }
                }
                standardScore.setScore(blacklist.getScore());
                turnoverVo.setViolationScore(standardScore);
            } else {
                turnoverVo.setType(BlackStatusEnum.NONE.code);
            }
        }

        return Result.ok(turnoverVo);
    }

    @ApiOperation("查询出站站点的进站点统计")
    @GetMapping("listInStationDetail")
    public Result listInStationDetail(@RequestParam Integer carId, @RequestParam Integer stationId, @RequestParam String beginMonth) {
        EntityUtil.dateMonthChecked(beginMonth);
        List<TurnoverStationDto> turnoverStationDtos = tietouService.listInStationDetail(carId, stationId, beginMonth);
        return Result.ok(turnoverStationDtos);
    }

    @ApiOperation(value = "查询车辆风险详细通行记录",notes = "type，1：路径异常风险，2：短途重载风险，3：长途轻载风险，4：行驶速度异常风险<br/>" +
            "5：同站进出风险，6：进出车型不一致风险，7：进出车情不一致风险，8：轴数异常风险，9：进出车牌不一致,10：5分钟内先出后进，11：通行时间重合，12路段标志缺失，" +
            "13：高速异常风险，14：低速异常风险")
    @PostMapping("listRiskInOutDetail")
    public Result listRiskInOutDetail(@RequestBody @Validated(value = {AddValid.class, PageValid.class}) RiskInOutDto riskInOutDto){
//        EntityUtil.dateMonthChecked(riskInOutDto.getBeginMonth());
        PageVo pageVo;
        if (riskInOutDto.getBeginDate() == null) {
            riskInOutDto.setBeginDate(DateUtils.getFirstDayOfCurrentYear());
        }
        if (riskInOutDto.getEndDate() == null) {
            riskInOutDto.setEndDate(DateUtils.getCurrentDay());
        }
        //把前台传入的里程由km转化为m
        if (riskInOutDto.getMaxDistance() != null) {
            riskInOutDto.setMaxDistance(riskInOutDto.getMaxDistance() * 1000);
        }
        if (riskInOutDto.getMinDistance() != null) {
            riskInOutDto.setMinDistance(riskInOutDto.getMinDistance() * 1000);
        }
        if (riskInOutDto.getCode() == FeatureCodeEnum.MIN_OUT_IN.code){
            pageVo = tietouSameStationFrequentlyService.listByQuery(riskInOutDto);
        }else {
            pageVo = tietouService.listRiskInOutDetail(riskInOutDto);
        }
        return Result.ok(pageVo);
    }

    @ApiOperation("查询车辆风险类型分布-按数量降序")
    @PostMapping("listRiskByRank")
    public Result listRiskByRank(@RequestBody @Validated(value = {AddValid.class, PageValid.class})RiskByRankRequest riskByRankRequest){
        List<RiskMap> riskMaps = tietouService.listRiskByRank(riskByRankRequest);
        return Result.ok(riskMaps);
    }

    @ApiOperation(value = "查询车辆在车站的通行频次",notes = "flag:0为高速通行频次，1为进出站关系图")
    @GetMapping("listThroughFrequency")
    public Result listThroughFrequency(@Validated({QueryValid.class,PageValid.class})CarMonthQueryDto dto){
        PageVo<ThroughFrequencyDto> pageVo = tietouService.listThroughFrequency(dto);
        return Result.ok(pageVo);
    }

    @ApiOperation("查询车辆的风险等级")
    @GetMapping("getCarRiskLevel")
    public Result getCarRiskLevel(@RequestParam Integer carId,@RequestParam Integer beginMonth){
        BigDecimal score = tietouScoreGyhService.getCarRiskLevel(carId, beginMonth);
        return Result.ok(score);
    }

    @ApiOperation("查询车辆每天异常次数统计")
    @GetMapping("listPeriodViolationAmount")
    public Result listPeriodViolationAmount(@RequestParam Integer carId,@RequestParam String beginMonth,@RequestParam Integer type){
        List<PeriodAmountDto> periodAmountDto = tietouService.listPeriodViolationAmountAction(carId, beginMonth,type);
        return Result.ok(periodAmountDto);
    }

    @ApiOperation("轴数统计")
    @GetMapping("getAxlenum")
    public Result getAxlenum(@RequestParam Integer carId,@RequestParam String beginMonth){
        List<PeriodAmountDto> periodAmountDto = tietouService.getAxlenum(carId);
        return Result.ok(periodAmountDto);
    }

    @ApiOperation("查询车站路段速度异常记录")
    @GetMapping("listStationSpeedInfo")
    public Result listStationSpeedInfo(@Validated(PageValid.class) StationSpeedQueryDto queryDto){
        return Result.ok(stationFeatureStatisticService.listStationSpeedInfo(queryDto));
    }

    @ApiOperation("查询车辆的车型分布")
    @GetMapping("listCarTypeDetail")
    public Result listCarTypeDetail(@RequestParam Integer vlpId,@RequestParam Integer isCurrent){
        List<PeriodAmountDto> vcDtos = tietouService.listCarTypeDetail(vlpId, 1,isCurrent);
        List<PeriodAmountDto> envcDtos = tietouService.listCarTypeDetail(vlpId, 2, isCurrent);
        CarTypeDistributionVo carTypeDistributionVo = new CarTypeDistributionVo();
        carTypeDistributionVo.setInType(envcDtos);
        carTypeDistributionVo.setOutType(vcDtos);
        return Result.ok(carTypeDistributionVo);
    }

    @ApiOperation(value = "根据车辆id获取车辆详细信息", notes = "carId:车辆id")
    @GetMapping("getCarDetails")
    public Result getCarDetails(@RequestParam Integer carId){

        CarDetailResponse response = tietouService.getCarDetail(carId);
        return Result.ok(response);
    }
}
