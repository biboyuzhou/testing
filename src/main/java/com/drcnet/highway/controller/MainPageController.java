package com.drcnet.highway.controller;

import com.drcnet.highway.constants.TipsConsts;
import com.drcnet.highway.dto.PeriodAmountDto;
import com.drcnet.highway.dto.RiskAmountDto;
import com.drcnet.highway.dto.RiskPeriodAmount;
import com.drcnet.highway.dto.request.*;
import com.drcnet.highway.dto.response.*;
import com.drcnet.highway.entity.TietouFeatureStatisticGyh;
import com.drcnet.highway.entity.TietouOrigin;
import com.drcnet.highway.entity.dic.TietouCarDic;
import com.drcnet.highway.exception.MyException;
import com.drcnet.highway.service.*;
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
import java.util.ArrayList;
import java.util.Arrays;
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
    @Resource
    private TietouInboundService inboundService;


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
    @ApiOperation(value = "查询违规列表",notes = "carType:-1查询所有，0货车，1客车; 开始日期和结束日期筛选<br>" +
            "minDistance、minDistance最小最大里程（单位千米）<br>minTravelTime、maxTravelTime最小最大行程时间（单位分钟）")
    public Result listCheatingCarByTime(@Validated({QueryValid.class, PageValid.class}) CheatingListTimeSearchDto dto){
        if (dto.getPageSize() > 300) {
            return Result.error("数据量太大");
        }
        PageVo<TietouFeatureStatisticGyh> pageVo;
        if (!dto.isTietouQuery(dto) && !dto.isExtractionQuery(dto)) {
            pageVo = tietouScoreGyhService.listCheatingCarByTimeOnDefaultQery(dto, dto.getCarType());
        } else if (!dto.isTietouQuery(dto) && dto.isExtractionQuery(dto)) {
            pageVo = tietouScoreGyhService.listCheatingCarByTimeWithExtraction(dto);
        } else if (dto.isTietouQuery(dto) && !dto.isExtractionQuery(dto)) {
            pageVo = tietouScoreGyhService.listCheatingCarByTimeWithTietou(dto);
        } else {
            pageVo = tietouScoreGyhService.listCheatingCarByTime(dto);
        }
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
    public Result getDiffCarNoStatic(@RequestParam Integer carId, @RequestParam Integer isCurrent){
        DiffCarNoStaticDto diffCarNoStaticDto = tietouService.getDiffCarNoStatic(carId, isCurrent);
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
//    @PermissionCheck(AuthConsts.ALL)
    public Result uploadSqlFile(MultipartFile file,@RequestHeader String secret){
        //密钥
        String serverSecret = "qwer!@#$";
        if (!serverSecret.equals(secret)){
             return Result.error("请输入正确的密钥");
        }
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

    /**
     * 获取换卡风险数据
     * @return
     */
    @ApiOperation(value = "获取换卡风险数据", notes = " 进口卡号:inCard<br>出口卡号:outCard<br>进口车牌号inCarNo<br>出口车牌号outCarNo<br>" +
            "入口开始日期:beginDate<br>入口结束日期:endDate<br>换卡确认(0：待定；1：换卡):changeCardConfirm<br>分页查询页数:pageNum<br>每页数量:pageSize")
    @GetMapping("getChangeCardList")
    public Result getChangeCardList(ChangeCardQueryDto dto){
        if (dto.getPageSize() != null && dto.getPageSize() > 300) {
            return Result.error("数据量太大");
        }
        if (!StringUtils.isEmpty(dto.getBeginDate())) {
            EntityUtil.dateChecked(dto.getBeginDate());
        }

        if (!StringUtils.isEmpty(dto.getEndDate())) {
            EntityUtil.dateChecked(dto.getEndDate());
        }
        if (dto.getPageNum() == null) {
            dto.setPageNum(1);
        }
        if (dto.getPageSize() == null) {
            dto.setPageSize(10);
        }

        try {
            PageVo pageVo = inboundService.getChangeCardList(dto);
            return Result.ok(pageVo);
        } catch (ParseException e) {
            throw new MyException("日期格式转化失败！");
        }
    }

    /**
     * 将换卡风险数据状态由待定更改为换卡
     * @return
     */
    @ApiOperation(value = "将换卡风险数据状态由待定更改为换卡", notes = " 入口数据id:enId<br>state:将数据改为状态值")
    @GetMapping("updateConfirmState")
    public Result updateConfirmState(Integer enId, Integer state){
        if (enId == null || state == null) {
            return Result.error("enId和state不能为空!");
        }

        int result = inboundService.updateConfirmState(enId, state);
        return Result.ok(result);
    }

    /**
     * 根据出口车牌和分页pageSize获取当前数据id在该车牌行程记录的分页pageNum
     * @return
     */
    @ApiOperation(value = "根据出口车牌和分页pageSize获取当前数据id在该车牌行程记录的分页pageNum", notes = "recordId:当前数据id<br>carId:车辆id<br>pageSize:每页数量")
    @GetMapping("getPageNumByCarId")
    public Result getPageNumByCarId(@Validated({QueryValid.class})TravelRecordPageNumDto dto){
        PageNumByCardIdResponse response = tietouService.getPageNumByCarId(dto);
        return Result.ok(response);
    }

    /**
     * 根据idList获取tietou数据
     * @return
     */
    @ApiOperation(value = "根据idList获取tietou数据", notes = "idList:id的集合")
    @GetMapping("getTieTouByIdList")
    public Result getTieTouByIdList(String ids){
        List<Integer> idList = new ArrayList<>();
        if (ids.contains(",")) {
            String[] idArray = ids.split(",");
            List<String> idStrList = Arrays.asList(idArray);
            idStrList.stream().forEach(s -> {
                idList.add(Integer.parseInt(s));
            });
        } else {
            Integer id = Integer.parseInt(ids);
            idList.add(id);
        }

        List<TietouOrigin> originList = tietouService.getTieTou2019ByIdList(idList);
        return Result.ok(originList);
    }

    /**
     * 根据idList获取tietou数据
     * @return
     */
    @ApiOperation(value = "根据时间区间获取tietou2019数据", notes = "carId:车牌id, startTime:开始时间，endTime:结束时间")
    @PostMapping("queryTravelRecordsByTime")
    public Result queryTravelRecordsByTime(@RequestBody TravelRecordQueryDto travelRecordQueryDto){
        try {
            List<TietouOrigin> originList = tietouService.getTieTou2019ByTime(travelRecordQueryDto);
            PageVo pageVo = PageVo.of(originList);
            return Result.ok(pageVo);
        } catch (Exception e) {
            throw new MyException("日期格式转化失败！");
        }

    }

}
