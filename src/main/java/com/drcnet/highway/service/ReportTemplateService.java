package com.drcnet.highway.service;

import com.drcnet.highway.constants.CarSituationConsts;
import com.drcnet.highway.constants.RgbConsts;
import com.drcnet.highway.constants.TimeConsts;
import com.drcnet.highway.constants.enumtype.YesNoEnum;
import com.drcnet.highway.dao.StationDicMapper;
import com.drcnet.highway.dao.TietouCarDicMapper;
import com.drcnet.highway.dao.TietouFeatureStatisticMapper;
import com.drcnet.highway.dto.PeriodAmountDto;
import com.drcnet.highway.dto.request.CheatingListTimeSearchDto;
import com.drcnet.highway.dto.request.RiskInOutDto;
import com.drcnet.highway.entity.TietouFeatureStatistic;
import com.drcnet.highway.entity.TietouOrigin;
import com.drcnet.highway.entity.TietouSameStationFrequently;
import com.drcnet.highway.entity.dic.StationDic;
import com.drcnet.highway.entity.dic.TietouCarDic;
import com.drcnet.highway.enums.FeatureCodeEnum;
import com.drcnet.highway.enums.FeatureEnum;
import com.drcnet.highway.util.DateUtils;
import com.drcnet.highway.util.DocxUtil;
import com.drcnet.highway.util.NumberUtil;
import com.drcnet.highway.vo.PageVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.drcnet.highway.constants.CarSituationConsts.CAR_TYPE_MAP;
import static com.drcnet.highway.util.DocxUtil.*;
import static com.drcnet.highway.util.domain.ParagraphBuilder.createParagraph;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/1 10:36
 * @Description:
 */
@Service
@Slf4j
public class ReportTemplateService {

    @Resource
    private TietouFeatureStatisticMapper featureStatisticMapper;
    @Resource
    private TietouSameStationFrequentlyService tietouSameStationFrequentlyService;
    @Resource
    private TietouCarDicMapper carDicMapper;
    @Resource
    private TietouService tietouService;
    @Resource
    private StationDicMapper stationDicMapper;

    /**
     * 创建报告
     */
    public XWPFDocument buildReport(Collection<Integer> carNoIds, String title, CheatingListTimeSearchDto dto, YesNoEnum isCurrent) {
        if (CollectionUtils.isEmpty(carNoIds)) {
            return null;
        }

        Map<Integer, String> vcMap = CAR_TYPE_MAP.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        XWPFDocument document = new XWPFDocument();


        //将文档设置为横向
        crosswiseDocx(document);

        //设置页眉
        buildPageHeader(document, "高速公路逃费风险报告");
        //创建封面
        buildDocumentCovers(document, title);
        //创建报告说明
        buildReportExplain(document, dto);

        int orderNum = 0;
        //生成文档内容
        for (Integer carNoId : carNoIds) {
            orderNum++;
            buildContent(orderNum, carNoId, document, vcMap, dto,isCurrent);
        }
        //设置行距
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            DocxUtil.setSingleLineSpacing(paragraph, 360);
        }
        //格式化表格
        formatTables(document);
        return document;
    }

    /**
     * 创建单个车辆的违规内容
     *  @param orderNum 排序号
     * @param carNoId  车牌ID
     * @param document 文档
     * @param vcMap    车型Map
     * @param dto
     * @param isCurrent
     */
    private void buildContent(int orderNum, Integer carNoId, XWPFDocument document, Map<Integer, String> vcMap, CheatingListTimeSearchDto dto, YesNoEnum isCurrent) {
        TietouFeatureStatistic statisticQuery = new TietouFeatureStatistic();
        statisticQuery.setVlpId(carNoId);
        TietouFeatureStatistic statisticRes;
        if (isCurrent == YesNoEnum.YES){
            statisticRes = featureStatisticMapper.selectOne(statisticQuery);
        }else {
            statisticRes = featureStatisticMapper.selectFromAllByVlpId(carNoId);
        }

        if (statisticRes == null) {
            return;
        }
        TietouCarDic car = carDicMapper.selectById(carNoId);
        //生成基本信息和违规信息
        buildBasicMsgAndCheatingMsg(orderNum, car, document, vcMap, statisticRes);
        //生成异常报告
        int cheatingCount = 1;
        Integer lowSpeedAmount = statisticRes.getLowSpeed();
        Integer differentZhou = statisticRes.getDifferentZhou();
        Integer sameCarNumber = statisticRes.getSameCarNumber();
        Integer shortDisOverweight = statisticRes.getShortDisOverweight();
        Integer longDisLightweight = statisticRes.getLongDisLightweight();
        Integer sameTimeRangeAgain = statisticRes.getSameTimeRangeAgain();
        Integer minOutIn = statisticRes.getMinOutIn();
        Integer carType = car.getCarType();
        int lowSpeedFlag = 2;
        int minOutInFlag = 2;
        //如果速度过慢次数超过10次,且短途重载和长途轻载小于10次，则生成速度过慢报告
        if (lowSpeedAmount > lowSpeedFlag) {
            buildLowSpeed(cheatingCount, statisticRes, carNoId, document, dto,isCurrent);
            cheatingCount++;
            //用于打印速度速度异常说明
            /*boolean speedFlag = false;
            if ((shortDisOverweight < 5 && longDisLightweight < 5 && minOutIn < minOutInFlag) || carType < 10) {
                buildLowSpeed(cheatingCount, statisticRes, carNoId, document, dto);
                speedFlag = true;
                cheatingCount++;
            }*/

            /*if (carType > 10 && (shortDisOverweight >= 5 || longDisLightweight >= 5)) {
                cheatingCount = buildLowSpeedAndOverWeight(cheatingCount, statisticRes, carNoId, document);
                speedFlag = true;
            }
            //如果高频进出次数超过10次，则记为一个异常点
            if (carType > 10 && minOutIn >= minOutInFlag) {
                cheatingCount = buildFrequentlyOutIn(cheatingCount, statisticRes, carNoId, document, speedFlag);
            }*/

        }
        if (carType > 10 && longDisLightweight >= 5) {
            //长途轻载
            buildLongDisLightweight(cheatingCount, statisticRes, carNoId, document, dto,isCurrent);
            cheatingCount++;
        }


        //短途重载
        if (carType > 10 && shortDisOverweight >= 5) {
            buildShortDisOverweight(cheatingCount, statisticRes, carNoId, document, dto,isCurrent);
            cheatingCount++;
        }
        //轴数不一致超过2次则生成轴数异常报告
        if (carType > 10 && differentZhou >= 2) {
            buildDifferentZhou(cheatingCount, statisticRes, carNoId, document);
            cheatingCount++;
        }
        //高频进出
        if (carType > 10 && minOutIn > minOutInFlag) {
            buildMinOutIn(cheatingCount, statisticRes, carNoId, document,isCurrent);
            cheatingCount++;
        }
        //生成车牌不一致报告
        if (sameCarNumber >= 2) {
            buildSameCarNumber(cheatingCount, statisticRes, carNoId, document,isCurrent);
            cheatingCount++;
        }
        //生成时间重叠报告
        if (sameTimeRangeAgain > 2) {
            buildSameTimeRangeAgain(cheatingCount, statisticRes, carNoId, document, dto,isCurrent);
        }
        //翻页
        document.createParagraph().createRun().addBreak(BreakType.PAGE);
    }


    /**
     * 生成基本信息和违规信息
     */
    private void buildBasicMsgAndCheatingMsg(int orderNum, TietouCarDic car, XWPFDocument document, Map<Integer, String> vcMap, TietouFeatureStatistic statisticRes) {
        String text = orderNum + "、车牌:" + car.getCarNo();
        //车牌
        XWPFRun run = createParagraphAndRun(document, text, ParagraphAlignment.LEFT, CALIBRI, 15, true);
        CTShd ctShd = run.getCTR().addNewRPr().addNewShd();
        ctShd.setFill(RgbConsts.BLACK);
        run.setColor(RgbConsts.WHITE);
        //基本信息
        String messageText = orderNum + ".1、基本信息:";
        XWPFRun messageRun = createParagraphAndRun(document, messageText, ParagraphAlignment.LEFT, FANG_SONG, 14, true);
        messageRun.setColor(RgbConsts.RED);
        //生成基本信息表格，包含 车型、轴数、风险、1-5月通行次数
        XWPFTable table = document.createTable(2, 4);
        //设置单元格宽度
        DocxUtil.setTableWidth(table, 12000);
        XWPFTableRow titleRow = table.getRow(0);
        XWPFTableRow contentRow = table.getRow(1);
        createParagraph(titleRow.getCell(0).addParagraph()).setTxt("车型").build();
        createParagraph(titleRow.getCell(1).addParagraph()).setTxt("轴数").build();
        createParagraph(titleRow.getCell(2).addParagraph()).setTxt("风险").build();
        createParagraph(titleRow.getCell(3).addParagraph()).setTxt("1-8月通行次数").build();

        createParagraph(contentRow.getCell(0).addParagraph()).setTxt(vcMap.get(car.getCarType())).build();
        createParagraph(contentRow.getCell(1).addParagraph()).setTxt(Optional.ofNullable(car.getAxlenum()).map(String::valueOf).orElse("")).build();
        createParagraph(contentRow.getCell(2).addParagraph()).setTxt(String.valueOf(statisticRes.getScore())).build();
        createParagraph(contentRow.getCell(3).addParagraph()).setTxt(String.valueOf(statisticRes.getTransitTimes())).build();


        //生成违规次数表格
        String cheatingAmountText = orderNum + ".2、异常次数:";
        XWPFRun cheatingAmountRun = createParagraphAndRun(document, cheatingAmountText, ParagraphAlignment.LEFT, FANG_SONG, 14, true);
        cheatingAmountRun.setColor(RgbConsts.RED);

        XWPFTable cheatingAmountTable = document.createTable(2, 8);
        DocxUtil.setTableWidth(cheatingAmountTable, 12000);
        XWPFTableRow cheatingTitleRow = cheatingAmountTable.getRow(0);
        XWPFTableRow cheatingContentRow = cheatingAmountTable.getRow(1);
        createParagraph(cheatingTitleRow.getCell(0).addParagraph()).setTxt("轴数异常").build();
        createParagraph(cheatingTitleRow.getCell(1).addParagraph()).setTxt("车牌不一致").build();
        createParagraph(cheatingTitleRow.getCell(2).addParagraph()).setTxt("时间重叠").build();
        createParagraph(cheatingTitleRow.getCell(3).addParagraph()).setTxt("车速过快").build();
        createParagraph(cheatingTitleRow.getCell(4).addParagraph()).setTxt("车速过慢").build();
        createParagraph(cheatingTitleRow.getCell(5).addParagraph()).setTxt("长途轻载").build();
        createParagraph(cheatingTitleRow.getCell(6).addParagraph()).setTxt("短途重载").build();
        createParagraph(cheatingTitleRow.getCell(7).addParagraph()).setTxt("同站先出后进").build();

        createParagraph(cheatingContentRow.getCell(0).addParagraph()).setTxt(String.valueOf(statisticRes.getDifferentZhou())).build();
        createParagraph(cheatingContentRow.getCell(1).addParagraph()).setTxt(String.valueOf(statisticRes.getSameCarNumber())).build();
        createParagraph(cheatingContentRow.getCell(2).addParagraph()).setTxt(String.valueOf(statisticRes.getSameTimeRangeAgain())).build();
        createParagraph(cheatingContentRow.getCell(3).addParagraph()).setTxt(String.valueOf(statisticRes.getHighSpeed())).build();
        createParagraph(cheatingContentRow.getCell(4).addParagraph()).setTxt(String.valueOf(statisticRes.getLowSpeed())).build();
        createParagraph(cheatingContentRow.getCell(5).addParagraph()).setTxt(String.valueOf(statisticRes.getLongDisLightweight())).build();
        createParagraph(cheatingContentRow.getCell(6).addParagraph()).setTxt(String.valueOf(statisticRes.getShortDisOverweight())).build();
        createParagraph(cheatingContentRow.getCell(7).addParagraph()).setTxt(String.valueOf(statisticRes.getMinOutIn())).build();

    }

    /**
     * 生成低速报告
     */
    private void buildLowSpeed(int cheatingCount, TietouFeatureStatistic statistic, Integer carNoId, XWPFDocument document, CheatingListTimeSearchDto dto, YesNoEnum isCurrent) {
        Integer lowSpeedAmount = statistic.getLowSpeed();
        Integer transitTimes = statistic.getTransitTimes();
        String lowSpeedTips = getTxt(FeatureEnum.LOW_SPEED, cheatingCount, lowSpeedAmount, getPercent(lowSpeedAmount, transitTimes, 1));
        String lowSpeedDetail = getTxt(FeatureEnum.LOW_SPEED_DETAIL, lowSpeedAmount);
        //设置标题和说明
        settingTitleAndDetail(lowSpeedTips, lowSpeedDetail, document);

        //查询速度过慢数据
        RiskInOutDto inOutDto = new RiskInOutDto();
        inOutDto.setCarId(carNoId);
        inOutDto.setCode(FeatureCodeEnum.LOW_SPEED.code);
        inOutDto.setPageNum(1);
        inOutDto.setPageSize(10);
        inOutDto.setIsCurrent(isCurrent.getCode());
        inOutDto.setBeginDate(dto.getBeginDate());
        inOutDto.setEndDate(dto.getEndDate());
        PageVo<TietouOrigin> lowSpeedRecord = tietouService.listRiskInOutDetail(inOutDto);
        List<TietouOrigin> data = lowSpeedRecord.getData();
        XWPFTable table = document.createTable(data.size() + 1, 6);
        setTableWidth(table, 12000);
        //设置标题
        XWPFTableRow titleRow = table.getRow(0);
        createParagraph(titleRow.getCell(0).addParagraph()).setTxt("进站口").build();
        createParagraph(titleRow.getCell(1).addParagraph()).setTxt("出站口").build();
        createParagraph(titleRow.getCell(2).addParagraph()).setTxt("进站时间").build();
        createParagraph(titleRow.getCell(3).addParagraph()).setTxt("出站时间").build();
        createParagraph(titleRow.getCell(4).addParagraph()).setTxt("通行距离(km)").build();
        createParagraph(titleRow.getCell(5).addParagraph()).setTxt("平均速度(km/h)").build();
//        createParagraph(titleRow.getCell(6).addParagraph()).setTxt("该路段该车型平均速度(km/h)").build();


        for (int i = 0; i < data.size(); i++) {
            TietouOrigin record = data.get(i);
            XWPFTableRow row = table.getRow(i + 1);
            LocalDateTime entime = record.getEntime();
            LocalDateTime extime = record.getExtime();
            createParagraph(row.getCell(0).addParagraph()).setTxt(record.getRk()).build();
            createParagraph(row.getCell(1).addParagraph()).setTxt(record.getCk()).build();
            createParagraph(row.getCell(2).addParagraph()).setTxt(DateUtils.formatTime(entime)).build();
            createParagraph(row.getCell(3).addParagraph()).setTxt(DateUtils.formatTime(extime)).build();
            createParagraph(row.getCell(4).addParagraph()).setTxt(String.valueOf(NumberUtil.divideThousand(record.getTolldistance(), 3))).build();
            createParagraph(row.getCell(5).addParagraph()).setTxt(String.valueOf(getDivide((double) record.getTolldistance(), (double) Duration.between(entime, extime).getSeconds(), 2))).build();
//            createParagraph(row.getCell(6).addParagraph()).setTxt(Optional.ofNullable(record.getStationFeature())
//                    .map(var -> String.valueOf(var.getAvgSpeedByVc(record.getVc()))).orElse("")).build();
        }
    }

    /**
     * 当出现多次车速过低和短途重载/长途轻载时
     *
     * @param cheatingCount 违规次数
     * @param statistic     统计结果
     * @param carNoId       车牌ID
     * @param document      doc文档
     */
    private int buildLowSpeedAndOverWeight(int cheatingCount, TietouFeatureStatistic statistic, Integer carNoId, XWPFDocument document) {
        Integer lowSpeedAmount = statistic.getLowSpeed();
        Integer shortDisOverweight = statistic.getShortDisOverweight();
        Integer longDisLightweight = statistic.getLongDisLightweight();
        Integer transitTimes = statistic.getTransitTimes();
        String featureName;
        boolean overWeightFlag = false;
        boolean lightWeightFlag = false;
        if (shortDisOverweight >= 5 && longDisLightweight >= 5) {
            featureName = "短途重载/长途轻载";
            overWeightFlag = true;
            lightWeightFlag = true;
        } else if (shortDisOverweight >= 5) {
            featureName = "短途重载";
            overWeightFlag = true;
        } else {
            featureName = "长途轻载";
            lightWeightFlag = true;
        }

        //车速异常统计
        buildStatisticContent(cheatingCount++, "车速过慢", lowSpeedAmount, getPercent(lowSpeedAmount, transitTimes, 2), document, FeatureEnum.LOW_SPEED_DETAIL);
        //短途重载统计
        if (overWeightFlag)
            buildStatisticContent(cheatingCount++, "短途重载", shortDisOverweight, getPercent(shortDisOverweight, transitTimes, 2), document, FeatureEnum.SHORT_DIS_OVERWEIGHT_DETAIL);
        //长途轻载
        if (lightWeightFlag)
            buildStatisticContent(cheatingCount++, "长途轻载", longDisLightweight, getPercent(longDisLightweight, transitTimes, 2), document, FeatureEnum.LONG_DIS_LIGHTWEIGHT_DETAIL);


//        String tips = getTxt(FeatureEnum.LOW_SPEED_WEIGHT, cheatingCount, featureName, lowSpeedAmount, featureAmount);
        String detail = getTxt(FeatureEnum.LOW_SPEED_WEIGHT_DETAIL, featureName);

        //设置标题和说明
        settingTitleAndDetail(null, detail, document);
        //载重说明
        buildWeightTips(carNoId, document);

        List<TietouOrigin> tietouOrigins = tietouService.listLowSpeedAndWeight(carNoId, 10, overWeightFlag, lightWeightFlag);
        tietouService.setRouteAvgSpeed(tietouOrigins);
        XWPFTable table = document.createTable(tietouOrigins.size() + 1, 7);
        setTableWidth(table, 12000);
        //设置标题
        XWPFTableRow titleRow = table.getRow(0);
        createParagraph(titleRow.getCell(0).addParagraph()).setTxt("进站口").build();
        createParagraph(titleRow.getCell(1).addParagraph()).setTxt("出站口").build();
        createParagraph(titleRow.getCell(2).addParagraph()).setTxt("进站时间").build();
        createParagraph(titleRow.getCell(3).addParagraph()).setTxt("出站时间").build();
        createParagraph(titleRow.getCell(4).addParagraph()).setTxt("通行距离(km)").build();
        createParagraph(titleRow.getCell(5).addParagraph()).setTxt("平均速度(km/h)").build();
//        createParagraph(titleRow.getCell(6).addParagraph()).setTxt("该路段该车型平均速度").build();
        createParagraph(titleRow.getCell(6).addParagraph()).setTxt("总重(t)").build();

        for (int i = 0; i < tietouOrigins.size(); i++) {
            TietouOrigin record = tietouOrigins.get(i);
            Integer tolldistance = record.getTolldistance();
            XWPFTableRow row = table.getRow(i + 1);
            LocalDateTime entime = record.getEntime();
            LocalDateTime extime = record.getExtime();
            long distance = Duration.between(entime, extime).getSeconds();
            double speed = getDivide(Optional.ofNullable(tolldistance).orElse(0) * 3.6, distance, 2);
            createParagraph(row.getCell(0).addParagraph()).setTxt(record.getRk()).build();
            createParagraph(row.getCell(1).addParagraph()).setTxt(record.getCk()).build();
            createParagraph(row.getCell(2).addParagraph()).setTxt(DateUtils.formatTime(entime)).build();
            createParagraph(row.getCell(3).addParagraph()).setTxt(DateUtils.formatTime(extime)).build();
            createParagraph(row.getCell(4).addParagraph()).setTxt(String.valueOf(NumberUtil.divideThousand(record.getTolldistance(), 3))).build();
            createParagraph(row.getCell(5).addParagraph()).setTxt(String.valueOf(speed)).build();
//            createParagraph(row.getCell(6).addParagraph()).setTxt(Optional.ofNullable(record.getStationFeature())
//                    .map(var -> String.valueOf(var.getAvgSpeedByVc(record.getVc()))).orElse("")).build();
            createParagraph(row.getCell(6).addParagraph()).setTxt(String.valueOf(NumberUtil.divideThousand(record.getTotalweight(), 3))).build();
        }
        return cheatingCount;
    }

    /**
     * 设置高频进出异常点
     */
    private int buildFrequentlyOutIn(int cheatingCount, TietouFeatureStatistic statisticRes, Integer carNoId, XWPFDocument document, boolean speedFlag,YesNoEnum isCurrent) {
        Integer minOutIn = statisticRes.getMinOutIn();
        Integer lowSpeed = statisticRes.getLowSpeed();
        Integer transitTimes = statisticRes.getTransitTimes();
        if (!speedFlag){}
            buildStatisticContent(cheatingCount++, "车速过慢", lowSpeed, getPercent(lowSpeed, transitTimes, 2), document, FeatureEnum.LOW_SPEED_DETAIL);
        //高频进出统计
        buildStatisticContent(cheatingCount++, "同站先出后进", minOutIn, getPercent(minOutIn, transitTimes, 2), document, FeatureEnum.MIN_OUT_IN_DETAIL);
//        String title = getTxt(FeatureEnum.LOW_SPEED_FREQUENCY_IN_OUT, cheatingCount, lowSpeed, minOutIn, getPercent(lowSpeed, transitTimes, 1), getPercent(minOutIn, transitTimes, 2));
        String detail = getTxt(FeatureEnum.LOW_SPEED_FREQUENCY_IN_OUT_DETAIL);
        settingTitleAndDetail(null, detail, document, "  该车辆同站先出后进通行记录如下所示:");
        buildMinOutInTable(carNoId, document, isCurrent);
        return cheatingCount;
    }

    /**
     * 生成高频进出报告信息
     */
    private void buildMinOutIn(int cheatingCount, TietouFeatureStatistic statisticRes, Integer carNoId, XWPFDocument document, YesNoEnum isCurrent) {
        String title = getTxt(FeatureEnum.MIN_OUT_IN, cheatingCount, statisticRes.getMinOutIn());
        String detail = getTxt(FeatureEnum.MIN_OUT_IN_DETAIL);
        settingTitleAndDetail(title, detail, document, "  该车辆同站先出后进通行记录如下所示:");
        buildMinOutInTable(carNoId, document,isCurrent);
    }

    /**
     * 生成先出后进表格
     *  @param carNoId  车牌ID
     * @param document 文档
     * @param isCurrent
     */
    private void buildMinOutInTable(Integer carNoId, XWPFDocument document, YesNoEnum isCurrent) {
        RiskInOutDto inOutDto = new RiskInOutDto();
        inOutDto.setCarId(carNoId);
        inOutDto.setCode(FeatureCodeEnum.MIN_OUT_IN.code);
        inOutDto.setPageNum(1);
        inOutDto.setPageSize(10);
        inOutDto.setIsCurrent(isCurrent.getCode());
        inOutDto.setBeginDate("2019-01-01");
        inOutDto.setEndDate(LocalDate.now().format(DateTimeFormatter.ofPattern(TimeConsts.DATE_FORMAT)));
        PageVo<TietouSameStationFrequently> pageVo = tietouSameStationFrequentlyService.listByQuery(inOutDto);
        List<TietouSameStationFrequently> data = pageVo.getData();
        XWPFTable table = document.createTable(data.size() + 1, 9);
        setTableWidth(table, 12000);
        //设置标题
        XWPFTableRow titleRow = table.getRow(0);
        createParagraph(titleRow.getCell(0).addParagraph()).setTxt("上次进站时间").build();
        createParagraph(titleRow.getCell(1).addParagraph()).setTxt("上次进站站点").build();
        createParagraph(titleRow.getCell(2).addParagraph()).setTxt("上次通行距离(km)").build();
        createParagraph(titleRow.getCell(3).addParagraph()).setTxt("上次出站时间").build();
        createParagraph(titleRow.getCell(4).addParagraph()).setTxt("本次入站时间").build();
        createParagraph(titleRow.getCell(5).addParagraph()).setTxt("本次出站站点").build();
        createParagraph(titleRow.getCell(6).addParagraph()).setTxt("本次出站时间").build();
        createParagraph(titleRow.getCell(7).addParagraph()).setTxt("本次通行距离(km)").build();
        createParagraph(titleRow.getCell(8).addParagraph()).setTxt("进出站站点").build();

        for (int i = 0; i < data.size(); i++) {
            TietouSameStationFrequently frequentlyData = data.get(i);
            XWPFTableRow row = table.getRow(i + 1);
            createParagraph(row.getCell(0).addParagraph()).setTxt(DateUtils.formatTime(frequentlyData.getLastEntime())).build();
            createParagraph(row.getCell(1).addParagraph()).setTxt(frequentlyData.getLastInStationName()).build();
            createParagraph(row.getCell(2).addParagraph()).setTxt(String.valueOf(NumberUtil.divideThousand(frequentlyData.getLastDistance(), 3))).build();
            createParagraph(row.getCell(3).addParagraph()).setTxt(DateUtils.formatTime(frequentlyData.getOutTime())).build();
            createParagraph(row.getCell(4).addParagraph()).setTxt(DateUtils.formatTime(frequentlyData.getInTime())).build();
            createParagraph(row.getCell(5).addParagraph()).setTxt(frequentlyData.getNextOutStationName()).build();
            createParagraph(row.getCell(6).addParagraph()).setTxt(String.valueOf(frequentlyData.getNextExtime())).build();
            createParagraph(row.getCell(7).addParagraph()).setTxt(String.valueOf(NumberUtil.divideThousand(frequentlyData.getNextDistance(), 3))).build();
            createParagraph(row.getCell(8).addParagraph()).setTxt(String.valueOf(frequentlyData.getTollStationName())).build();

        }
        //在表格末尾添加说明
        XWPFParagraph entTipsParagraph = document.createParagraph();
        XWPFRun entTipsRun = entTipsParagraph.createRun();
        entTipsRun.setText("本次入站时间和上次出站时间");
        entTipsRun.setFontSize(9);
        entTipsRun.setBold(true);
        entTipsRun.setColor("FF0000");
        XWPFRun run2 = entTipsParagraph.createRun();
        run2.setText("间隔小于5分钟，则视为先出后进异常通行记录；");
        run2.setFontSize(9);

        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run3 = paragraph.createRun();
        run3.setText("进出站站点");
        run3.setFontSize(9);
        run3.setBold(true);
        run3.setColor("FF0000");
        XWPFRun run4 = paragraph.createRun();
        run4.setText("为先出后进所在站点");
        run4.setFontSize(9);
        setFontFamily(paragraph, FANG_SONG);
        setFontFamily(entTipsParagraph, FANG_SONG);
    }


    /**
     * 生成轴数异常报告
     */
    private void buildDifferentZhou(int cheatingCount, TietouFeatureStatistic statisticRes, Integer carNoId, XWPFDocument document) {
        String title = getTxt(FeatureEnum.DIFFERENT_ZHOU, cheatingCount, statisticRes.getDifferentZhou());
        String detail = getTxt(FeatureEnum.DIFFERENT_ZHOU_DETAIL);
        settingTitleAndDetail(title, detail, document, "该车辆轴数和通行次数统计如下:");
        List<PeriodAmountDto> axlenumStatistics = tietouService.getAxlenum(carNoId);

        XWPFTable table = document.createTable(axlenumStatistics.size() + 1, 2);
        setTableWidth(table, 4000);
        XWPFTableRow titleRow = table.getRow(0);
        createParagraph(titleRow.getCell(0).addParagraph()).setTxt("轴数").build();
        createParagraph(titleRow.getCell(1).addParagraph()).setTxt("通行次数").build();
        for (int i = 0; i < axlenumStatistics.size(); i++) {
            PeriodAmountDto amountDto = axlenumStatistics.get(i);
            XWPFTableRow row = table.getRow(i + 1);
            createParagraph(row.getCell(0).addParagraph()).setTxt(amountDto.getPeriod()).build();
            createParagraph(row.getCell(1).addParagraph()).setTxt(String.valueOf(amountDto.getAmount())).build();
        }
    }

    /**
     * 生成车牌不一致报告
     */
    private void buildSameCarNumber(int cheatingCount, TietouFeatureStatistic statisticRes, Integer carNoId, XWPFDocument document, YesNoEnum isCurrent) {
        String title = getTxt(FeatureEnum.SAME_CAR_NUMBER, cheatingCount, statisticRes.getSameCarNumber());
        String detail = getTxt(FeatureEnum.SAME_CAR_NUMBER_DETAIL);
        settingTitleAndDetail(title, detail, document);
        List<TietouOrigin> tietouOrigins = tietouService.listSameCarNumRecord(carNoId, 10,isCurrent.getCode());
        XWPFTable table = document.createTable(tietouOrigins.size() + 1, 6);
        setTableWidth(table, 12000);
        XWPFTableRow titleRow = table.getRow(0);
        createParagraph(titleRow.getCell(0).addParagraph()).setTxt("进站时间").build();
        createParagraph(titleRow.getCell(1).addParagraph()).setTxt("进站口").build();
        createParagraph(titleRow.getCell(2).addParagraph()).setTxt("出站时间").build();
        createParagraph(titleRow.getCell(3).addParagraph()).setTxt("出站口").build();
        createParagraph(titleRow.getCell(4).addParagraph()).setTxt("进站车牌").build();
        createParagraph(titleRow.getCell(5).addParagraph()).setTxt("出站车牌").build();

        for (int i = 0; i < tietouOrigins.size(); i++) {
            TietouOrigin tietouOrigin = tietouOrigins.get(i);
            XWPFTableRow row = table.getRow(i + 1);
            createParagraph(row.getCell(0).addParagraph()).setTxt(DateUtils.formatTime(tietouOrigin.getEntime())).build();
            createParagraph(row.getCell(1).addParagraph()).setTxt(tietouOrigin.getRk()).build();
            createParagraph(row.getCell(2).addParagraph()).setTxt(DateUtils.formatTime(tietouOrigin.getExtime())).build();
            createParagraph(row.getCell(3).addParagraph()).setTxt(tietouOrigin.getCk()).build();
            createParagraph(row.getCell(4).addParagraph()).setTxt(tietouOrigin.getEnvlp()).build();
            createParagraph(row.getCell(5).addParagraph()).setTxt(tietouOrigin.getVlp()).build();
        }

        List<TietouOrigin> envlpRecord = new ArrayList<>();
        for (TietouOrigin tietouOrigin : tietouOrigins) {
            LocalDateTime entime = tietouOrigin.getEntime();
            LocalDateTime extime = tietouOrigin.getExtime();
            LocalDateTime startTime = entime.toLocalDate().atStartOfDay();
            LocalDateTime endTime = extime.toLocalDate().plusDays(1).atStartOfDay();
            Integer envlpId = tietouOrigin.getEnvlpId();
            List<TietouOrigin> res = tietouService.listRecordByVlpAndExEntime(envlpId, startTime, endTime);
            envlpRecord.addAll(res);
        }
        if (!envlpRecord.isEmpty()){
            settingTitleAndDetail(null, detail, document,"进站车牌部分通行记录如下:");
            XWPFTable enVlpTable = document.createTable(envlpRecord.size() + 1, 6);
            setTableWidth(enVlpTable, 12000);
            XWPFTableRow enVlpTitleRow = enVlpTable.getRow(0);
            createParagraph(enVlpTitleRow.getCell(0).addParagraph()).setTxt("进站时间").build();
            createParagraph(enVlpTitleRow.getCell(1).addParagraph()).setTxt("进站口").build();
            createParagraph(enVlpTitleRow.getCell(2).addParagraph()).setTxt("出站时间").build();
            createParagraph(enVlpTitleRow.getCell(3).addParagraph()).setTxt("出站口").build();
            createParagraph(enVlpTitleRow.getCell(4).addParagraph()).setTxt("进站车牌").build();
            createParagraph(enVlpTitleRow.getCell(5).addParagraph()).setTxt("出站车牌").build();

            for (int i = 0; i < envlpRecord.size(); i++) {
                TietouOrigin tietouOrigin = envlpRecord.get(i);
                XWPFTableRow row = enVlpTable.getRow(i + 1);
                createParagraph(row.getCell(0).addParagraph()).setTxt(DateUtils.formatTime(tietouOrigin.getEntime())).build();
                createParagraph(row.getCell(1).addParagraph()).setTxt(tietouOrigin.getRk()).build();
                createParagraph(row.getCell(2).addParagraph()).setTxt(DateUtils.formatTime(tietouOrigin.getExtime())).build();
                createParagraph(row.getCell(3).addParagraph()).setTxt(tietouOrigin.getCk()).build();
                createParagraph(row.getCell(4).addParagraph()).setTxt(tietouOrigin.getEnvlp()).build();
                createParagraph(row.getCell(5).addParagraph()).setTxt(tietouOrigin.getVlp()).build();
            }
        }

    }

    /**
     * 生成时间重叠报告
     */
    private void buildSameTimeRangeAgain(int cheatingCount, TietouFeatureStatistic statisticRes, Integer carNoId, XWPFDocument document, CheatingListTimeSearchDto dto, YesNoEnum isCurrent) {
        String title = getTxt(FeatureEnum.SAME_TIME_RANGE_AGAIN, cheatingCount, statisticRes.getSameTimeRangeAgain());
        String detail = getTxt(FeatureEnum.SAME_TIME_RANGE_AGAIN_DETAIL);
        settingTitleAndDetail(title, detail, document, "  该车辆时间重叠通行记录如下");
        PageVo<TietouOrigin> pageVo = tietouService.listRiskInOutDetail(carNoId, FeatureCodeEnum.SAME_TIME_RANGE_AGAIN, dto, isCurrent);
        List<TietouOrigin> data = pageVo.getData();
        XWPFTable table = document.createTable(data.size() + 1, 5);
        setTableWidth(table, 12000);
        XWPFTableRow titleRow = table.getRow(0);
        createParagraph(titleRow.getCell(0).addParagraph()).setTxt("入口时间").build();
        createParagraph(titleRow.getCell(1).addParagraph()).setTxt("出口时间").build();
        createParagraph(titleRow.getCell(2).addParagraph()).setTxt("入口站").build();
        createParagraph(titleRow.getCell(3).addParagraph()).setTxt("出口站").build();
        createParagraph(titleRow.getCell(4).addParagraph()).setTxt("时间重叠ID").build();

        for (int i = 0; i < data.size(); i++) {
            TietouOrigin tietouOrigin = data.get(i);
            XWPFTableRow row = table.getRow(i + 1);
            createParagraph(row.getCell(0).addParagraph()).setTxt(DateUtils.formatTime(tietouOrigin.getEntime())).build();
            createParagraph(row.getCell(1).addParagraph()).setTxt(DateUtils.formatTime(tietouOrigin.getExtime())).build();
            createParagraph(row.getCell(2).addParagraph()).setTxt(tietouOrigin.getRk()).build();
            createParagraph(row.getCell(3).addParagraph()).setTxt(tietouOrigin.getCk()).build();
            createParagraph(row.getCell(4).addParagraph()).setTxt(tietouOrigin.getSameRouteMark()).build();
        }
    }

    /**
     * 生成长途轻载报告
     */
    private void buildLongDisLightweight(int cheatingCount, TietouFeatureStatistic statisticRes, Integer carNoId, XWPFDocument document, CheatingListTimeSearchDto dto, YesNoEnum isCurrent) {
        String title = getTxt(FeatureEnum.LONG_DIS_LIGHTWEIGHT, cheatingCount, statisticRes.getLongDisLightweight());
        String detail = getTxt(FeatureEnum.LONG_DIS_LIGHTWEIGHT_DETAIL);
        //载重说明
        buildWeightTips(carNoId, document);
        settingTitleAndDetail(title, detail, document, "  该车辆长途轻载通行记录如下");
        PageVo<TietouOrigin> pageVo = tietouService.listRiskInOutDetail(carNoId, FeatureCodeEnum.LONG_DIS_LIGHTWEIGHT, dto,isCurrent);
        List<TietouOrigin> data = pageVo.getData();
        //生成表格
        overAndLightWeight(data, document);
    }

    /**
     * 生成短途重载信息
     */
    private void buildShortDisOverweight(int cheatingCount, TietouFeatureStatistic statisticRes, Integer carNoId, XWPFDocument document, CheatingListTimeSearchDto dto, YesNoEnum isCurrent) {
        String title = getTxt(FeatureEnum.SHORT_DIS_OVERWEIGHT, cheatingCount, statisticRes.getShortDisOverweight());
        String detail = getTxt(FeatureEnum.SHORT_DIS_OVERWEIGHT_DETAIL);
        //载重说明
        buildWeightTips(carNoId, document);
        settingTitleAndDetail(title, detail, document, "  该车辆短途重载通行记录如下");
        PageVo<TietouOrigin> pageVo = tietouService.listRiskInOutDetail(carNoId, FeatureCodeEnum.SHORT_DIS_OVERWEIGHT, dto, isCurrent);
        List<TietouOrigin> data = pageVo.getData();
        //生成表格
        overAndLightWeight(data, document);
    }


    /**
     * 设置标题和说明
     *
     * @param title    标题
     * @param detail   说明
     * @param document 文档
     */
    private void settingTitleAndDetail(String title, String detail, XWPFDocument document, String nextWord) {
        XWPFParagraph paragraph = document.createParagraph();
        if (title != null) {
            XWPFRun tipsRun = paragraph.createRun();
            tipsRun.addBreak();
            tipsRun.setText(title);
            tipsRun.setBold(true);
        }
        //fixme 注释了异常特征推断信息
//        XWPFParagraph paragraph2 = document.createParagraph();
//        XWPFRun detailRun = paragraph2.createRun();
//        detailRun.setText(detail);
        XWPFParagraph paragraph3 = document.createParagraph();
        XWPFRun noticeRun = paragraph3.createRun();
        if (nextWord == null) {
            nextWord = "  该车辆部分通行记录如下:";
        }
        noticeRun.setText(nextWord);
        noticeRun.setBold(true);
        setFontFamily(paragraph, FANG_SONG);
//        setFontFamily(paragraph2, FANG_SONG);
        setFontFamily(paragraph3, FANG_SONG);
    }

    private void settingTitleAndDetail(String title, String detail, XWPFDocument document) {
        settingTitleAndDetail(title, detail, document, null);
    }

    /**
     * 生成载重说明
     */
    private void buildWeightTips(Integer carNoId, XWPFDocument document) {
        TietouCarDic tietouCarDic = carDicMapper.selectById(carNoId);
        String weightTips = getTxt(FeatureEnum.WEIGHT_HISTORY, NumberUtil.divideThousand(tietouCarDic.getWeightMin(), 3), NumberUtil.divideThousand(tietouCarDic.getWeightMax(), 3));
        XWPFParagraph weightTipsParagraph = document.createParagraph();
        XWPFRun weightTipsRun = weightTipsParagraph.createRun();
        weightTipsRun.addBreak();
        weightTipsRun.setText(weightTips);
        weightTipsRun.setBold(true);
        setFontFamily(weightTipsParagraph, FANG_SONG);
    }

    /**
     * 短途重载和长途轻载表格生成
     */
    private void overAndLightWeight(List<TietouOrigin> data, XWPFDocument document) {
        XWPFTable table = document.createTable(data.size() + 1, 6);
        setTableWidth(table, 12000);
        XWPFTableRow titleRow = table.getRow(0);
        createParagraph(titleRow.getCell(0).addParagraph()).setTxt("入口时间").build();
        createParagraph(titleRow.getCell(1).addParagraph()).setTxt("出口时间").build();
        createParagraph(titleRow.getCell(2).addParagraph()).setTxt("入口站").build();
        createParagraph(titleRow.getCell(3).addParagraph()).setTxt("出口站").build();
        createParagraph(titleRow.getCell(4).addParagraph()).setTxt("里程(km)").build();
        createParagraph(titleRow.getCell(5).addParagraph()).setTxt("总重(t)").build();

        for (int i = 0; i < data.size(); i++) {
            TietouOrigin tietouOrigin = data.get(i);
            XWPFTableRow row = table.getRow(i + 1);
            createParagraph(row.getCell(0).addParagraph()).setTxt(DateUtils.formatTime(tietouOrigin.getEntime())).build();
            createParagraph(row.getCell(1).addParagraph()).setTxt(DateUtils.formatTime(tietouOrigin.getExtime())).build();
            createParagraph(row.getCell(2).addParagraph()).setTxt(tietouOrigin.getRk()).build();
            createParagraph(row.getCell(3).addParagraph()).setTxt(tietouOrigin.getCk()).build();
            createParagraph(row.getCell(4).addParagraph()).setTxt(String.valueOf(NumberUtil.divideThousand(tietouOrigin.getTolldistance(), 3))).build();
            createParagraph(row.getCell(5).addParagraph()).setTxt(String.valueOf(NumberUtil.divideThousand(tietouOrigin.getTotalweight(), 3))).build();
        }
    }

    /**
     * 生成统计文本
     */
    private void buildStatisticContent(Integer cheatingCount, String cheatingMsg, Integer amount, double proportion, XWPFDocument document, FeatureEnum featureEnum) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun msgRun = paragraph.createRun();
        msgRun.addBreak();
        msgRun.setText(getTxt(FeatureEnum.CHEATING_MSG, cheatingCount, cheatingMsg));
        msgRun.setBold(true);

        XWPFParagraph countParagraph = document.createParagraph();
        XWPFRun countingRun = countParagraph.createRun();
        countingRun.setBold(true);
        countingRun.setText(getTxt(FeatureEnum.COUNT_MSG, amount));

        XWPFParagraph proportionParagraph = document.createParagraph();
        XWPFRun proportionRun = proportionParagraph.createRun();
        proportionRun.setBold(true);
        proportionRun.setText(getTxt(FeatureEnum.COUNT_PROPORTION_MSG, proportion));
        /*if (featureEnum != null) {
            XWPFParagraph detailParagraph = document.createParagraph();
            XWPFRun explainRun = detailParagraph.createRun();
            explainRun.setText(getTxt(featureEnum));
            setFontFamily(detailParagraph, FANG_SONG);
        }*/
        setFontFamily(paragraph, FANG_SONG);
        setFontFamily(countParagraph, FANG_SONG);
        setFontFamily(proportionParagraph, FANG_SONG);
    }

    /**
     * 设置段落的字体
     */
    private void setFontFamily(XWPFParagraph paragraph, String fontFamily) {
        if (paragraph == null) {
            return;
        }
        List<XWPFRun> runs = paragraph.getRuns();
        for (XWPFRun run : runs) {
            run.setFontFamily(fontFamily);
        }
    }


    /**
     * 将文档设置为横向
     */
    private void crosswiseDocx(XWPFDocument document) {
        if (document == null)
            return;
        CTPageSz ctPageSz = document.getDocument().getBody().addNewSectPr().addNewPgSz();
        ctPageSz.setW(BigInteger.valueOf(15840));
        ctPageSz.setH(BigInteger.valueOf(11907));
        ctPageSz.setOrient(STPageOrientation.LANDSCAPE);
    }

    /**
     * 创建报告封面
     */
    private void buildDocumentCovers(XWPFDocument document, String title) {
        XWPFRun run = createParagraphAndRun(document, ParagraphAlignment.CENTER, null, 28, false);
        run.addBreak();
        run.addBreak();

        XWPFRun titleRun = createParagraphAndRun(document, ParagraphAlignment.CENTER, HEI_TI, 28, true);
        titleRun.setText(title);

        XWPFRun reportRun = createParagraphAndRun(document, ParagraphAlignment.CENTER, HEI_TI, 21, true);
        reportRun.addBreak();
        reportRun.setText("风险报告");


        LocalDate now = LocalDate.now();
        String date = now.getYear() + "年" + now.getMonth().getValue() + "月";
        XWPFRun dateRun = createParagraphAndRun(document, ParagraphAlignment.CENTER, FANG_SONG, 18, false);
        dateRun.addBreak();
        dateRun.addBreak();
        dateRun.addBreak();
        dateRun.setText(date);
        dateRun.addBreak(BreakType.PAGE);
    }


    /**
     * 设置标题
     *
     * @param document 文档
     * @param title    标题
     */
    private void buildPageHeader(XWPFDocument document, String title) {
        XWPFHeader header = document.createHeader(HeaderFooterType.DEFAULT);
        XWPFParagraph paragraph = header.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.RIGHT);
        XWPFRun run = paragraph.createRun();
        run.setFontFamily(HUA_WEN_XI_HEI);
        run.setText(title);
        run.setFontSize(9);
    }

    /**
     * 设置报告说明
     *
     * @param document 文档对象
     */
    private void buildReportExplain(XWPFDocument document, CheatingListTimeSearchDto dto) {
        String beginDate = dto.getBeginDate();
        String endDate = dto.getEndDate();
        Integer rkId = dto.getRkId();
        Integer ckId = dto.getCkId();
        Integer riskFlag = dto.getRiskFlag();
        Integer axleNum = dto.getAxleNum();
        String timePeriod;
        if (beginDate == null && endDate == null) {
            timePeriod = "数据时间段:无限制";
        } else {
            timePeriod = getTxt(FeatureEnum.TIME_PERIOD, beginDate, endDate);
        }
        String carType = Optional.ofNullable(CarSituationConsts.CAR_TYPE_MAP_REV.get(dto.getCarDetailType())).orElse("全部车型");
        String riskLevel;
        if (riskFlag == 0) {
            riskLevel = "高风险";
        } else if (riskFlag == 1) {
            riskLevel = "中风险";
        } else {
            riskLevel = "低风险";
        }
        String rk = "未限制";
        String ck = "未限制";
        if (rkId != null) {
            StationDic stationDic = stationDicMapper.selectById(rkId);
            rk = Optional.ofNullable(stationDic).map(StationDic::getStationName).orElse("无限制");
        }
        if (ckId != null) {
            StationDic stationDic = stationDicMapper.selectById(ckId);
            ck = Optional.ofNullable(stationDic).map(StationDic::getStationName).orElse("无限制");
        }
        String axleStr = "未限制";
        if (axleNum != null && axleNum > 0) {
            axleStr = String.valueOf(axleNum);
        }
        //标题
        createParagraphAndRun(document, "报告说明", ParagraphAlignment.CENTER, FANG_SONG, 22, true);
        //说明
        createParagraphAndRun(document, getTxt(FeatureEnum.REPORT_EXPLAIN1), true, ParagraphAlignment.LEFT, FANG_SONG, 12, false);
        createParagraphAndRun(document, getTxt(FeatureEnum.REPORT_EXPLAIN2), true, ParagraphAlignment.LEFT, FANG_SONG, 12, false);

        createParagraphAndRun(document, "数据说明：", true, ParagraphAlignment.LEFT, FANG_SONG, 12, true);
        createParagraphAndRun(document, "风险点：全部风险", true, ParagraphAlignment.LEFT, FANG_SONG, 12, false);
        createParagraphAndRun(document, "车辆类型：" + carType, true, ParagraphAlignment.LEFT, FANG_SONG, 12, false);
        createParagraphAndRun(document, "风险级别：" + riskLevel, true, ParagraphAlignment.LEFT, FANG_SONG, 12, false);
        createParagraphAndRun(document, timePeriod, true, ParagraphAlignment.LEFT, FANG_SONG, 12, false);
        createParagraphAndRun(document, "入口站：" + rk, true, ParagraphAlignment.LEFT, FANG_SONG, 12, false);
        createParagraphAndRun(document, "出口站：" + ck, true, ParagraphAlignment.LEFT, FANG_SONG, 12, false);
        XWPFRun run = createParagraphAndRun(document, "轴数：" + axleStr, true, ParagraphAlignment.LEFT, FANG_SONG, 12, false);
        run.addBreak(BreakType.PAGE);

    }

    private void formatTables(XWPFDocument document) {
        for (XWPFTable table : document.getTables()) {
            boolean firstRow = true;
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell tableCell : row.getTableCells()) {
                    if (firstRow) {
                        CTTc cttc = tableCell.getCTTc();
                        CTTcPr ctTcPr = cttc.addNewTcPr();
                        ctTcPr.addNewShd().setFill(RgbConsts.BLUE);
                    }
                    for (XWPFParagraph paragraph : tableCell.getParagraphs()) {
                        paragraph.setAlignment(ParagraphAlignment.CENTER);
                        List<XWPFRun> runs = paragraph.getRuns();
                        if (runs.isEmpty()) {
                            continue;
                        }
                        XWPFRun run = runs.get(0);
                        run.setFontFamily(FANG_SONG);
                        run.setFontSize(10);
                        if (firstRow) {
                            run.setBold(true);
                            run.setColor(RgbConsts.WHITE);
                        }
                    }
                }
                firstRow = false;
            }
        }
    }

}
