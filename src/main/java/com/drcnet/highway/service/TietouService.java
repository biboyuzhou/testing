package com.drcnet.highway.service;

import com.drcnet.highway.constants.CacheKeyConsts;
import com.drcnet.highway.constants.ConfigConsts;
import com.drcnet.highway.constants.RiskConsts;
import com.drcnet.highway.constants.TipsConsts;
import com.drcnet.highway.dao.*;
import com.drcnet.highway.dto.*;
import com.drcnet.highway.dto.request.*;
import com.drcnet.highway.dto.response.*;
import com.drcnet.highway.entity.StationFeatureStatistics;
import com.drcnet.highway.entity.TietouFeatureExtractionStandardScore;
import com.drcnet.highway.entity.TietouMonthStatistic;
import com.drcnet.highway.entity.TietouOrigin;
import com.drcnet.highway.entity.dic.TietouCarDic;
import com.drcnet.highway.enums.FeatureCodeEnum;
import com.drcnet.highway.exception.MyException;
import com.drcnet.highway.util.DateUtils;
import com.drcnet.highway.util.EntityUtil;
import com.drcnet.highway.util.Levenshtein;
import com.drcnet.highway.util.NumberUtil;
import com.drcnet.highway.vo.PageVo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/8 16:57
 * @Description:
 */
@Service
@Slf4j
public class TietouService {

    @Resource
    private TietouMapper tietouMapper;
    @Resource
    private TietouCarDicMapper tietouCarDicMapper;
    @Resource
    private TietouMonthStatisticMapper tietouMonthStatisticMapper;
    @Resource
    private TietouFeatureExtractionStandardScoreMapper tietouFeatureExtractionStandardScoreMapper;
    @Resource
    private StationFeatureStatisticsMapper stationFeatureStatisticsMapper;
    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private StationDicMapper stationDicMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private final String tablePrev = "first";
    private final String featureTablePrev = "tietou_feature_extraction_score";
    private final String extractionTablePrev = "tietou_feature_extraction_copy";
    private final String statisticTablePrev = "tietou_feature_extraction_statistic";


    /**
     * 查询车牌的进出站记录
     *
     * @param carId     车牌ID
     * @param monthTime
     */
    public List<TurnoverStationDto> listInAndOutStationAction(Integer carId, String monthTime) {
        List<TurnoverStationDto> inStations = tietouMapper.listInStation(carId);
        List<TurnoverStationDto> outStations = tietouMapper.listOutStation(carId);
        Map<String, TurnoverStationDto> inMap = inStations.stream().collect(Collectors.toMap(TurnoverStationDto::getStationName, var -> var));
        Map<String, TurnoverStationDto> outMap = outStations.stream().collect(Collectors.toMap(TurnoverStationDto::getStationName, var -> var));

        for (Map.Entry<String, TurnoverStationDto> inEntry : inMap.entrySet()) {
            String stationName = inEntry.getKey();
            TurnoverStationDto inTurnover = inEntry.getValue();
            TurnoverStationDto outTurnover = outMap.get(stationName);
            if (outTurnover != null) {
                inTurnover.setOutAmount(outTurnover.getOutAmount());
                outMap.remove(stationName);
            }
        }
        inMap.putAll(outMap);
        //按照进出站次数之和倒序排,加负号为倒序
        return inMap.values().stream()
                .sorted(Comparator
                        .comparing(var -> -(NumberUtil.nullFormat(var.getInAmount()) + NumberUtil.nullFormat(var.getOutAmount()))))
                .collect(Collectors.toList());
    }

    /**
     * 查询车牌的进出站记录
     *
     * @param carId     车牌ID
     * @param monthTime
     */
    @Async("taskExecutor")
    public Future<List<TurnoverStationDto>> listInAndOutStation(Integer carId, String monthTime) {
        List<TurnoverStationDto> turnoverStationDtos = listInAndOutStationAction(carId, monthTime);
        return AsyncResult.forValue(turnoverStationDtos);
    }

    /**
     * 查询
     *
     * @param carId
     * @param stationId
     * @param monthTime
     * @return
     */
    public List<TurnoverStationDto> listInStationDetail(Integer carId, Integer stationId, String monthTime) {
        String tableName = tablePrev + monthTime;
        return tietouMapper.listInStationDetail(carId, stationId, tableName);
    }

    /**
     * 查询车辆类型
     *
     * @param carId     车辆ID
     * @param monthTime
     * @return 车型
     */
    public Integer selectCarType(Integer carId, String monthTime) {
        String tableName = tablePrev + monthTime;
        return tietouMapper.selectCarTypeCode(carId, tableName);
    }

    /**
     * 查询车辆信息
     *
     * @param carId 车辆ID
     * @return
     */
    public TietouCarDic getCarInfoById(Integer carId) {
        return tietouCarDicMapper.selectByPrimaryKey(carId);
    }

    /**
     * 获得8大违规得分
     *
     * @param carId      车辆ID
     * @param beginMonth 月份
     * @return
     */
    public TietouFeatureExtractionStandardScore getMaxViolationScore(Integer carId, String beginMonth) {
        TietouFeatureExtractionStandardScore query = new TietouFeatureExtractionStandardScore();
        query.setMonthTime(Integer.valueOf(beginMonth));
        query.setCarNumId(carId);
        return tietouFeatureExtractionStandardScoreMapper.getMaxViolationScore(query);
    }

    /**
     * 查询路径异常次数
     *
     * @param carId      车牌ID
     * @param beginMonth 月份 yyyyMM
     */
    public List<PeriodAmountDto> listPeriodViolationAmountAction(Integer carId, String beginMonth, Integer type) {
        List<PeriodAmountDto> amountDtos = tietouMapper.listPeriodViolationAmount(carId, type);
        //当天没有数据，则填充为0
        fillBlankMonthPeriod(amountDtos);
        return amountDtos;
    }

    @Async("taskExecutor")
    public Future<List<PeriodAmountDto>> listPeriodViolationAmount(Integer carId, String beginMonth, Integer type) {
        List<PeriodAmountDto> periodAmountDtos = listPeriodViolationAmountAction(carId, beginMonth, type);
        return AsyncResult.forValue(periodAmountDtos);
    }

    /**
     * 查询车辆异常进出站详情列表
     *
     * @param riskInOutDto
     */
    public PageVo<TietouOrigin> listRiskInOutDetail(RiskInOutDto riskInOutDto) {
        PageHelper.startPage(riskInOutDto.getPageNum(), riskInOutDto.getPageSize());
        List<TietouOrigin> tietouOrigins = tietouMapper.listRiskInOutDetail(riskInOutDto.getCarId(), riskInOutDto.getCode());
        PageVo<TietouOrigin> pageVo = PageVo.of(tietouOrigins);
        List<TietouOrigin> dataList = pageVo.getData();
        if (riskInOutDto.getCode() == FeatureCodeEnum.SPEED.code || riskInOutDto.getCode() == FeatureCodeEnum.HIGH_SPEED.code
                || riskInOutDto.getCode() == FeatureCodeEnum.LOW_SPEED.code) {
            StationFeatureStatistics stationFeatureQuery = new StationFeatureStatistics();
            for (TietouOrigin tietouOrigin : dataList) {
                Integer rkId = tietouOrigin.getRkId();
                Integer ckId = tietouOrigin.getCkId();
                stationFeatureQuery.setCkId(ckId);
                stationFeatureQuery.setRkId(rkId);
                StationFeatureStatistics stationFeatureRes = stationFeatureStatisticsMapper.selectOne(stationFeatureQuery);
                Integer vc = tietouOrigin.getVc();
                stationFeatureRes.generateCurrentVcAvgSpeed(vc);
                tietouOrigin.setStationFeature(stationFeatureRes);
            }
        }
        return pageVo;
    }

    /**
     * 查询车辆风险类型频次分布，按次数降序
     *
     * @param carId      车辆ID
     * @param beginMonth 月份
     */
    public List<RiskMap> listRiskByRank(Integer carId, Integer beginMonth) {
        String tableName = tablePrev + beginMonth;
        String extractionName = extractionTablePrev + beginMonth;
        RiskAmountDto riskAmount = tietouMapper.getRiskAmount(carId, tableName, extractionName, beginMonth);
        Field[] fields = RiskAmountDto.class.getDeclaredFields();
        List<RiskMap> riskList = new ArrayList<>();
        for (Field field : fields) {
            if (field.getName().equalsIgnoreCase("serialVersionUID"))
                continue;
            String fieldName = field.getName();
            RiskMap riskMap = RiskConsts.RISK_MAP.get(fieldName);
            try {
                field.setAccessible(true);
                riskMap.setAmount((Integer) field.get(riskAmount));
            } catch (IllegalAccessException e) {
                log.error("{}", e);
                throw new MyException(TipsConsts.SERVER_ERROR);
            }
            riskList.add(riskMap);
        }
        riskList.sort(Comparator.comparing(RiskMap::getAmount).reversed());
        return riskList;
    }

    /**
     * 获得作弊车辆列表
     *
     * @param pageNum    页码
     * @param pageSize   页面大小
     * @param beginMonth
     * @return
     */
    public PageVo<CheatingViolationDto> listCheatingCar(Integer pageNum, Integer pageSize, Integer beginMonth) {
        String tableName = tablePrev + beginMonth;
        String scoreName = featureTablePrev + beginMonth;
        PageHelper.startPage(pageNum, pageSize);
        List<CheatingViolationDto> cheatingViolationDtos = tietouMapper.listCheatingCar(tableName, scoreName);
        return PageVo.of(cheatingViolationDtos);
    }

    /**
     * 查询某个月份的违规数量
     *
     * @param beginMonth
     * @param carType
     */
    public RiskAmountDto getCheatingCount(Integer beginMonth, Integer carType) {
        if (beginMonth == null) {
            beginMonth = 999999;
        }
        TietouMonthStatistic query = new TietouMonthStatistic();
        query.setMonthTime(beginMonth);
        query.setCarType(carType);
        query.setUseFlag(true);
        TietouMonthStatistic res = tietouMonthStatisticMapper.selectOne(query);
        if (res != null) {
            return EntityUtil.copyNotNullFields(res, new RiskAmountDto());
        }
        String extractionName = extractionTablePrev + beginMonth;
        String tableName = tablePrev + beginMonth;
        RiskAmountDto cheatingCount = tietouMapper.getCheatingCount(extractionName, tableName, carType);
        EntityUtil.copyNotNullFields(cheatingCount, query);
        tietouMonthStatisticMapper.insertSelective(query);
        return cheatingCount;
    }

    /**
     * 查询某个月份每天的违规数量
     *
     * @param beginMonth
     * @param carType
     * @return
     */
    @Cacheable(value = "cheatingPeriod", key = "#beginMonth.toString().concat('-').concat(#carType)")
    public List<PeriodAmountDto> listCheatingPeriod(String beginMonth, Integer carType) {
        /*String tableName = tablePrev + beginMonth;
        String extractionName = extractionTablePrev + beginMonth;
        String statisticName = statisticTablePrev + beginMonth;*/
        List<PeriodAmountDto> periodAmountDtos = new ArrayList<>(31);
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps(beginMonth);
        if (hashOperations.size() > 0) {
            Map<Object, Object> cacheMap = hashOperations.entries();
            for (Map.Entry<Object, Object> entry : cacheMap.entrySet()) {
                PeriodAmountDto periodAmountDto = new PeriodAmountDto();
                periodAmountDto.setPeriod(String.valueOf(entry.getKey()));
                periodAmountDto.setAmount(Integer.parseInt(String.valueOf(entry.getValue())));
                periodAmountDtos.add(periodAmountDto);
            }
        } else {
            Integer queryMonth = Integer.parseInt(beginMonth);
            periodAmountDtos = tietouMapper.listCheatingPeriod(carType, queryMonth);

            /*periodAmountDtos.stream().forEach(p -> {
                hashOperations.put(p.getPeriod(), p.getAmount());
            });*/
            //替换为更简单的lambda表达式
            if (!CollectionUtils.isEmpty(periodAmountDtos)) {
                periodAmountDtos.stream().forEach(p -> hashOperations.put(p.getPeriod(), p.getAmount()));
            }
        }
        fillBlankDayPeriod(periodAmountDtos, beginMonth);
        return periodAmountDtos;
    }

    /**
     * 将当日没有的数据补0
     */
    private void fillBlankDayPeriod(List<PeriodAmountDto> amountDtos, String beginMonth) {
        //先获得当月有多少天
        LocalDate date = LocalDate.parse(beginMonth + "01", DateTimeFormatter.ofPattern("yyyyMMdd"));
        int lengthOfMonth = date.lengthOfMonth();
        Map<String, PeriodAmountDto> dtoMap = amountDtos.stream().collect(Collectors.toMap(PeriodAmountDto::getPeriod, var -> var));

        //当天没有数据，则填充为0
        for (int i = 1; i <= lengthOfMonth; i++) {
            String day = String.valueOf(i);
            if (i < 10)
                day = 0 + day;
            String key = beginMonth + day;
            if (dtoMap.get(key) == null) {
                amountDtos.add(new PeriodAmountDto(key, 0));
            }
        }
        amountDtos.sort(Comparator.comparing(var -> Integer.parseInt(var.getPeriod())));
    }

    /**
     * 将当月没有的数据补0
     */
    private void fillBlankMonthPeriod(List<PeriodAmountDto> amountDtos) {
        Map<String, PeriodAmountDto> dtoMap = amountDtos.stream().collect(Collectors.toMap(PeriodAmountDto::getPeriod, var -> var));
        //当天没有数据，则填充为0
        for (int i = 201901; i <= 201912; i++) {
            String key = String.valueOf(i);
            if (dtoMap.get(key) == null) {
                int amount = i <= 201907 ? ThreadLocalRandom.current().nextInt(10) + 1 : 0;
                amountDtos.add(new PeriodAmountDto(key, amount));
            }
        }
        amountDtos.sort(Comparator.comparing(var -> Integer.parseInt(var.getPeriod())));
    }

    /**
     * 从所有表中查询通行记录
     *
     * @param queryDto 查询条件
     * @param carNoId  车牌ID
     */
    public PageVo<TietouOrigin> listDetailFromAllTimes(BlackDetailQueryDto queryDto, Integer carNoId) {
        List<String> tables = ConfigConsts.monthTimes.stream().map(var -> tablePrev + var).collect(Collectors.toList());
        PageHelper.startPage(queryDto.getPageNum(), queryDto.getPageSize());
        List<TietouOrigin> tietouOrigins = tietouMapper.listDetailFromAllTimes(queryDto, carNoId, tables);
        return PageVo.of(tietouOrigins);
    }

    /**
     * 标记通行记录
     *
     * @param markDto
     */
    public void markDetailList(DetailMarkDto markDto) {
        String tableName = tablePrev + markDto.getMonthTime();
        TietouOrigin tietouOrigin = tietouMapper.selectByTableNameAndId(tableName, markDto.getId());
        if (tietouOrigin == null) {
            throw new MyException(TipsConsts.NO_RECORD);
        }
        if (markDto.getFlag()) {
            if (tietouOrigin.getMark()) {
                throw new MyException("该记录已被标记，无法重复标记");
            }
            tietouOrigin.setMark(true);
            tietouMapper.updateMark(tietouOrigin, tableName);
        } else {
            if (!tietouOrigin.getMark()) {
                throw new MyException("该记录未被标记");
            }
            tietouOrigin.setMark(false);
            tietouMapper.updateMark(tietouOrigin, tableName);
        }
    }

    /**
     * 查询车辆进出站关系强度
     */
    public PageVo<ThroughFrequencyDto> listThroughFrequency(CarMonthQueryDto dto) {
        String tableName = tablePrev + dto.getBeginMonth();
        PageHelper.startPage(dto.getPageNum(), dto.getPageSize());
        List<ThroughFrequencyDto> dtos = new ArrayList<>();
        if (dto.getFlag() == 0) {
            //查询高速通行频次
            dtos = tietouMapper.listThroughFrequency(dto, tableName);
        } else if (dto.getFlag() == 1) {
            dtos = tietouMapper.listInOutStationRelation(dto, tableName);
        }
        return PageVo.of(dtos);
    }

    public Integer countThrough(Integer carId, String beginMonth) {
        return tietouMapper.countThrough(carId);
    }

    /**
     * 查询车轴分布
     *
     * @param carId
     * @return
     */
    public List<PeriodAmountDto> getAxlenum(Integer carId) {
        return tietouMapper.getAxlenum(carId);
    }

    /**
     * 查询当进出车牌不一致时，同一个进站车牌出现次数超过两次的记录
     *
     * @param vlpId
     */
    public List<SameCarEnvlpDto> listSameEnvlpOver2(Integer vlpId) {
        return tietouMapper.listSameEnvlpOver2(vlpId);
    }

    public List<SameCarEnvlpDto> listEnVlpByVlpId(Integer vlpId) {
        return tietouMapper.listEnVlpByVlpId(vlpId);
    }

    /**
     * 根据车牌、进口时间、出口时间、操作员查询通行记录
     *
     * @param travelRecordQueryDto
     * @return
     */
    public PageVo<TietouOrigin> queryTravelRecords(TravelRecordQueryDto travelRecordQueryDto) throws ParseException {
        PageHelper.startPage(travelRecordQueryDto.getPageNum(), travelRecordQueryDto.getPageSize());
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps("car_cache");

        Integer envlpId = null;
        if (!StringUtils.isEmpty(travelRecordQueryDto.getInCarNo())) {
            envlpId = (Integer) hashOperations.get(travelRecordQueryDto.getInCarNo());
            if (envlpId == null) {
                envlpId = getCarNoIdFromDb(travelRecordQueryDto.getInCarNo());
            }
        }
        Integer vlpId = null;
        if (!StringUtils.isEmpty(travelRecordQueryDto.getOutCarNo())) {
            vlpId = (Integer) hashOperations.get(travelRecordQueryDto.getOutCarNo());
            if (vlpId == null) {
                vlpId = getCarNoIdFromDb(travelRecordQueryDto.getOutCarNo());
            }
        }

        if (envlpId == null && vlpId == null && StringUtils.isEmpty(travelRecordQueryDto.getCard())) {
            return PageVo.of(Collections.EMPTY_LIST);
        }

        String inStartTime = "";
        String inEndTime = "";

        if (!StringUtils.isEmpty(travelRecordQueryDto.getInDate())) {
            inStartTime = new StringBuilder(DateUtils.convertDatePattern(travelRecordQueryDto.getInDate())).append(" 00:00:00").toString();
            inEndTime = new StringBuilder(DateUtils.convertDatePattern(travelRecordQueryDto.getInDate())).append(" 23:59:59").toString();
        }

        String outStartTime = "";
        String outEndTime = "";
        if (!StringUtils.isEmpty(travelRecordQueryDto.getOutDate())) {
            outStartTime = new StringBuilder(DateUtils.convertDatePattern(travelRecordQueryDto.getOutDate())).append(" 00:00:00").toString();
            outEndTime = new StringBuilder(DateUtils.convertDatePattern(travelRecordQueryDto.getOutDate())).append(" 23:59:59").toString();
        }

        List<TietouOrigin> tietouOriginList = tietouMapper.queryTravelRecords(envlpId, vlpId,
                inStartTime, inEndTime, outStartTime, outEndTime, travelRecordQueryDto.getOper(), travelRecordQueryDto.getCarType(),
                travelRecordQueryDto.getCard());

        return PageVo.of(tietouOriginList);
    }

    private Integer getCarNoIdFromDb(String carNo) {
        TietouCarDic carDic = tietouCarDicMapper.getIdByCarNo(carNo);
        if (carDic != null) {
            return carDic.getId();
        }
        return null;
    }

    /**
     * 统计指定车辆的进出车牌不一致数据
     *
     * @param carId
     * @return
     */
    public DiffCarNoStaticDto getDiffCarNoStatic(Integer carId) {
        List<DiffCarNoEnvlpDto> diffCarNoEnvlpDtoList = tietouMapper.stasticDiffCarNoByCarId(carId);
        List<DiffCarNoEnvlpDto> diffDtoList = new ArrayList<>(10);
        for (DiffCarNoEnvlpDto dto : diffCarNoEnvlpDtoList) {
            if (Levenshtein.calc(dto.getOutCarNo(), dto.getCarNo()) <= com.drcnet.highway.config.ConfigConsts.SAME_CAR_SCORE) {
                diffDtoList.add(dto);
            }
            if (diffDtoList.size() == 10) {
                break;
            }
        }
        int latchSize = diffDtoList.size();
        CountDownLatch latch = new CountDownLatch(latchSize);
        TietouService currentProxy = applicationContext.getBean(TietouService.class);
        for (DiffCarNoEnvlpDto dto : diffDtoList) {
            currentProxy.staticOutNumByEnvlp(dto, latch);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("{}", e);
            Thread.currentThread().interrupt();
        } finally {
            log.info("已统计完所有进站车牌的出站次数。");
        }
        DiffCarNoStaticDto staticDto = new DiffCarNoStaticDto();
        staticDto.setEnvlpDtoList(diffDtoList);
        if (!CollectionUtils.isEmpty(diffDtoList)) {
            Integer envlpId = diffDtoList.get(0).getCarId();
            int pageSize = diffDtoList.size() > 10 ? 10 : diffDtoList.size();
            PageVo<DiffCarNoInOutDataDto> pageVo = currentProxy.queryDiffCarNoInOutData(envlpId, carId, 1, pageSize);
            staticDto.setDiffCarNoInOutDataDtoPageVo(pageVo);
        }
        return staticDto;
    }

    /**
     * 根据进站车牌统计该车牌总的出站次数
     *
     * @param dto
     * @param latch
     */
    @Async("taskExecutor")
    public void staticOutNumByEnvlp(DiffCarNoEnvlpDto dto, CountDownLatch latch) {
        Integer outNum = tietouMapper.staticOutNumByEnvlp(dto.getCarId());
        dto.setOutNum(outNum);
        latch.countDown();
    }

    /**
     * 查询车辆的车型分布
     *
     * @param vlpId
     * @param flag  1为查询出站车牌，2为进站车牌
     */
    public List<PeriodAmountDto> listCarTypeDetail(Integer vlpId, Integer flag) {
        return tietouMapper.listCarTypeDetail(vlpId, flag);
    }

    /**
     * 根据进口和出口车牌分页查询进出口车牌不一致数据
     *
     * @param envlpId
     * @param vlpId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageVo<DiffCarNoInOutDataDto> queryDiffCarNoInOutData(Integer envlpId, Integer vlpId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<TietouOrigin> tietouOriginList = tietouMapper.listAllByEnvlpAndVlp(envlpId, vlpId);
        List<DiffCarNoInOutDataDto> dtoList = new ArrayList<>(tietouOriginList.size());
        TietouService currentProxy = applicationContext.getBean(TietouService.class);
        CountDownLatch latch = new CountDownLatch(tietouOriginList.size());
        for (TietouOrigin tietouOrigin : tietouOriginList) {
            currentProxy.queryOutTravelByEnvlp(tietouOrigin, dtoList, envlpId, latch);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("{}", e);
            Thread.currentThread().interrupt();
        } finally {
            log.info("已查询完所有进站车牌的出站数据。dtoList的总条数：{}", dtoList.size());
        }

        dtoList.sort(Comparator.comparing(DiffCarNoInOutDataDto::getInTime).reversed().
                thenComparing(DiffCarNoInOutDataDto::getInTime));
        return PageVo.of(dtoList);
    }

    /**
     * 根据入口车牌查与本次入口行程时间间隔最近的出口行程
     *
     * @param tietouOrigin
     * @param dtoList
     * @param vlpId
     * @param latch
     */
    @Async("taskExecutor")
    public void queryOutTravelByEnvlp(TietouOrigin tietouOrigin, List<DiffCarNoInOutDataDto> dtoList, Integer vlpId, CountDownLatch latch) {
        try {
            DiffCarNoInOutDataDto dto = new DiffCarNoInOutDataDto();
            dto.setInDistance(tietouOrigin.getTolldistance());
            dto.setInTime(tietouOrigin.getEntime());
            dto.setInStationName(tietouOrigin.getRk());
            dto.setInSpeed(tietouOrigin.getSpeed());
            dto.setInExlane(tietouOrigin.getExlane());
            dto.setInVc(tietouOrigin.getEnvc());

            LocalDateTime intime = dto.getInTime();
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime startTime = intime.minusDays(3);
            LocalDateTime endTime = intime.plusDays(3);
            String startTimeStr = df.format(intime);
            String endTimeStr = df.format(endTime);

            List<TietouOrigin> tietouOriginList = tietouMapper.queryOutTravelByEnvlp(vlpId, startTimeStr, endTimeStr);
            if (!CollectionUtils.isEmpty(tietouOriginList)) {
                TietouOrigin Origin = tietouOriginList.get(0);
                dto.setOutDistance(Origin.getTolldistance());
                dto.setOutExlane(Origin.getExlane());
                dto.setOutSpeed(Origin.getSpeed());
                dto.setOutStationName(Origin.getCk());
                dto.setOutTime(Origin.getExtime());
                dto.setOutVc(Origin.getVc());
            }
            dtoList.add(dto);
        } catch (Exception e) {
            log.error("根据入口车牌查与本次入口行程时间间隔最近的出口行程出错", e);
        } finally {
            latch.countDown();
        }

    }

    /**
     * 根据车牌查询综合风险
     *
     * @param carId
     * @return
     */
    public CompositeRiskDto queryCompositeRisk(Integer carId) {
        CompositeRiskDto compositeRiskDto = new CompositeRiskDto();
        CountDownLatch latch = new CountDownLatch(4);
        TietouService currentProxy = applicationContext.getBean(TietouService.class);
        int maxThreadNum = 4;
        for (int i = 0; i < maxThreadNum; i++) {
            currentProxy.getCompositeRiskData(carId, i, compositeRiskDto, latch);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("{}", e);
            Thread.currentThread().interrupt();
        } finally {
            log.info("已查询完所有进站车牌的出站数据。speedAndSameTimeList条数：{}, diffCarNoAndSpeedList：{}, diffCarNoAndSameTimeList：{}, allRiskList：{}",
                    compositeRiskDto.getSpeedAndSameTimeList().size(), compositeRiskDto.getDiffCarNoAndSpeedList().size(),
                    compositeRiskDto.getDiffCarNoAndSameTimeList().size(), compositeRiskDto.getAllRiskList().size());
        }
        return compositeRiskDto;
    }

    /**
     * 多线程获取综合风险数据
     *
     * @param carId
     * @param i
     * @param compositeRiskDto
     * @param latch
     */
    @Async("taskExecutor")
    public void getCompositeRiskData(Integer carId, int i, CompositeRiskDto compositeRiskDto, CountDownLatch latch) {
        try {
            switch (i) {
                case 1:
                    getDiffCarNoAndSameTimeList(carId, compositeRiskDto);
                    break;
                case 2:
                    getDiffCarNoAndSpeedList(carId, compositeRiskDto);
                    break;
                case 3:
                    getSpeedAndSameTimeList(carId, compositeRiskDto);
                    break;
                default:
                    getAllRiskList(carId, compositeRiskDto);
            }


        } catch (Exception e) {
            log.error("查询综合风险出错！", e);
        } finally {
            latch.countDown();
        }

    }

    /**
     * 速度异常、时间重叠风险
     *
     * @param carId
     * @param compositeRiskDto
     */
    private void getSpeedAndSameTimeList(Integer carId, CompositeRiskDto compositeRiskDto) {
        List<TietouOrigin> speedAndSameTimeList = tietouMapper.listSpeedAndSameTimeByVlpId(carId);
        compositeRiskDto.setSpeedAndSameTimeList(speedAndSameTimeList);
    }

    /**
     * 车牌不一致、速度异常风险
     *
     * @param carId
     * @param compositeRiskDto
     */
    private void getDiffCarNoAndSpeedList(Integer carId, CompositeRiskDto compositeRiskDto) {
        List<TietouOrigin> diffCarNoAndSpeedList = tietouMapper.listDiffCarNoAndSpeedByVlpId(carId);
        compositeRiskDto.setDiffCarNoAndSpeedList(diffCarNoAndSpeedList);
    }

    /**
     * 车牌不一致、时间重叠风险
     *
     * @param carId
     * @param compositeRiskDto
     */
    private void getDiffCarNoAndSameTimeList(Integer carId, CompositeRiskDto compositeRiskDto) {
        List<TietouOrigin> diffCarNoAndSameTimeList = tietouMapper.listDiffCarNoAndSameTimeByVlpId(carId);
        compositeRiskDto.setDiffCarNoAndSameTimeList(diffCarNoAndSameTimeList);
    }

    /**
     * 时间重叠、速度异常、车牌不一致综合风险
     *
     * @param carId
     * @param compositeRiskDto
     */
    private void getAllRiskList(Integer carId, CompositeRiskDto compositeRiskDto) {
        List<TietouOrigin> allRiskList = tietouMapper.listAllRiskByVlpId(carId);
        compositeRiskDto.setAllRiskList(allRiskList);
    }

    /**
     * 统计所有通行记录里每个车型的数量
     * 新版首页展示需要
     *
     * @return
     */
    public List<CommonTypeCountDto> statisticCarTypeCount() {
        List<CommonTypeCountDto> commonTypeCountDtoList = new ArrayList<>();
        //统计所有通行记录里每个车型的数量
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps("carType_count");
        if (hashOperations.size() > 0) {
            List<Object> objectList = hashOperations.values();
            for (Object o : objectList) {
                CommonTypeCountDto dto = new CommonTypeCountDto();
                LinkedHashMap<String, Object> linkedHashMap = (LinkedHashMap<String, Object>) o;
                dto.setType((Integer) linkedHashMap.get("type"));
                dto.setCount((Integer) linkedHashMap.get("count"));
                commonTypeCountDtoList.add(dto);
            }
        } else {
            commonTypeCountDtoList = tietouMapper.statisticCarTypeCount();
            Map<String, CommonTypeCountDto> map = new HashMap<>(commonTypeCountDtoList.size());
            for (CommonTypeCountDto commonTypeCountDto : commonTypeCountDtoList) {
                map.put(String.valueOf(commonTypeCountDto.getType()), commonTypeCountDto);
            }
            hashOperations.putAll(map);
        }
        return commonTypeCountDtoList;
    }

    /**
     * 统计二绕每个站点出的车辆总数、高中低风险数
     * 新版首页展示需要
     *
     * @return
     */
    public List<StationRiskCountDto> statistic2ndStationRiskCount() {
        List<StationRiskCountDto> riskCountDtoList = new ArrayList<>();
        //统计所有通行记录里每个车型的数量
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps(CacheKeyConsts.FIRST_PAGE_MAP_CACHE_KEY);
        if (hashOperations.size() > 0) {
            List<Object> objectList = hashOperations.values();
            for (Object o : objectList) {
                StationRiskCountDto dto = new StationRiskCountDto();
                LinkedHashMap<String, Object> linkedHashMap = (LinkedHashMap<String, Object>) o;
                dto.setCkId((Integer) linkedHashMap.get("ckId"));
                dto.setCkName((String) linkedHashMap.get("ckName"));
                dto.setLongitude((String) linkedHashMap.get("longitude"));
                dto.setLatitude((String) linkedHashMap.get("latitude"));
                dto.setTotal((Integer) linkedHashMap.get("total"));
                dto.setHigh((Integer) linkedHashMap.get("high"));
                dto.setMiddle((Integer) linkedHashMap.get("middle"));
                dto.setLow((Integer) linkedHashMap.get("low"));
                riskCountDtoList.add(dto);
            }
            riskCountDtoList.sort(Comparator.comparing(StationRiskCountDto::getHigh).reversed().thenComparing(StationRiskCountDto::getTotal));
        } else {
            riskCountDtoList = tietouMapper.statistic2ndStationRiskCount();
            List<StationRiskCountDto> stationDicList = stationDicMapper.list2ndStation();
            Map<String, StationRiskCountDto> map = new HashMap<>(riskCountDtoList.size());
            for (StationRiskCountDto dto : riskCountDtoList) {
                for (StationRiskCountDto stu : stationDicList) {
                    if (dto.getCkId().equals(stu.getCkId())) {
                        dto.setCkName(stu.getCkName());
                        dto.setLatitude(stu.getLatitude());
                        dto.setLongitude(stu.getLongitude());
                        break;
                    }
                }
                map.put(String.valueOf(dto.getCkId()), dto);
            }

            hashOperations.putAll(map);
        }

        return riskCountDtoList;
    }
}
