package com.drcnet.highway.service;

import com.drcnet.highway.config.LocalVariableConfig;
import com.drcnet.highway.constants.ConfigConsts;
import com.drcnet.highway.constants.RiskConsts;
import com.drcnet.highway.constants.TipsConsts;
import com.drcnet.highway.constants.enumtype.YesNoEnum;
import com.drcnet.highway.dao.*;
import com.drcnet.highway.domain.RiskByRankQuery;
import com.drcnet.highway.dto.*;
import com.drcnet.highway.dto.request.*;
import com.drcnet.highway.dto.response.*;
import com.drcnet.highway.entity.*;
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
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
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
    private TietouFeatureStatisticGyhMapper tietouFeatureStatisticGyhMapper;
    @Resource
    private StationFeatureStatisticsMapper stationFeatureStatisticsMapper;
    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private StationDicMapper stationDicMapper;
    @Resource
    private Tietou2019Mapper tietou2019Mapper;
    @Resource
    private TietouFeatureStatisticMapper tietouFeatureStatisticMapper;

    @Resource
    private LocalVariableConfig localVariableConfig;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private TietouSameStationFrequentlyMapper tietouSameStationFrequentlyMapper;

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
        return tietouCarDicMapper.selectById(carId);
    }

    /**
     * 获得8大违规得分
     *
     * @param carId      车辆ID
     * @return
     */
    public TietouFeatureStatisticGyh getMaxViolationScore(Integer carId) {
        TietouFeatureStatisticGyh query = new TietouFeatureStatisticGyh();
        query.setVlpId(carId);
        return tietouFeatureStatisticGyhMapper.selectOne(query);
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
        List<TietouOrigin> tietouOrigins ;
        if (riskInOutDto.getIsCurrent().equals(YesNoEnum.YES.getCode())) {
            tietouOrigins = tietouMapper.listRiskInOutDetail(riskInOutDto);
        } else {
            tietouOrigins = tietouMapper.listRiskInOutDetailFromAll(riskInOutDto);
        }
        PageVo<TietouOrigin> pageVo = PageVo.of(tietouOrigins);
        List<TietouOrigin> dataList = pageVo.getData();
        if (riskInOutDto.getCode() == FeatureCodeEnum.SPEED.code || riskInOutDto.getCode() == FeatureCodeEnum.HIGH_SPEED.code
                || riskInOutDto.getCode() == FeatureCodeEnum.LOW_SPEED.code) {
            setRouteAvgSpeed(dataList);
        }
        return pageVo;
    }

    /**
     * 设置tietouOrigin每条记录中，每个路段该车型的平均速度
     */
    public void setRouteAvgSpeed(List<TietouOrigin> dataList){
        for (TietouOrigin tietouOrigin : dataList) {
            Integer rkId = tietouOrigin.getRkId();
            Integer ckId = tietouOrigin.getCkId();
            StationFeatureStatistics stationFeatureRes = stationFeatureStatisticsMapper.selectByCkIdAndRkId(ckId, rkId);
            Integer vc = tietouOrigin.getVc();
            stationFeatureRes.generateCurrentVcAvgSpeed(vc);
            tietouOrigin.setStationFeature(stationFeatureRes);
        }
    }


    public PageVo<TietouOrigin> listRiskInOutDetail(Integer carNoId,FeatureCodeEnum featureCodeEnum,CheatingListTimeSearchDto dto) {
        RiskInOutDto riskInOutDto = new RiskInOutDto();
        riskInOutDto.setCarId(carNoId);
        riskInOutDto.setCode(featureCodeEnum.code);
        riskInOutDto.setPageNum(1);
        riskInOutDto.setPageSize(10);
        riskInOutDto.setIsCurrent(YesNoEnum.NO.getCode());
        riskInOutDto.setBeginDate(dto.getBeginDate());
        riskInOutDto.setEndDate(dto.getEndDate());
        return listRiskInOutDetail(riskInOutDto);
    }

    /**
     * 查询车辆风险类型频次分布，按次数降序
     *
     * @param riskByRankRequest
     */
    public List<RiskMap> listRiskByRank(RiskByRankRequest riskByRankRequest) {
        if (riskByRankRequest.getBeginDate() == null) {
            riskByRankRequest.setBeginDate(DateUtils.getFirstDayOfCurrentYear());
        }
        if (riskByRankRequest.getEndDate() == null) {
            riskByRankRequest.setEndDate(DateUtils.getCurrentDay());
        }
        //把前台传入的里程由km转化为m
        if (riskByRankRequest.getMaxDistance() != null) {
            riskByRankRequest.setMaxDistance(riskByRankRequest.getMaxDistance() * 1000);
        }
        if (riskByRankRequest.getMinDistance() != null) {
            riskByRankRequest.setMinDistance(riskByRankRequest.getMinDistance() * 1000);
        }
        RiskAmountDto riskAmount;
        RiskByRankQuery query = new RiskByRankQuery();
        query = EntityUtil.copyNotNullFields(riskByRankRequest, query);
        if (riskByRankRequest.getIsCurrent().equals(YesNoEnum.YES.getCode())) {
            riskAmount = tietouMapper.getRiskAmount(query);
        } else {
            riskAmount = tietouMapper.getRiskAmountFromAll(query);
        }
        Field[] fields = RiskAmountDto.class.getDeclaredFields();
        List<RiskMap> riskList = new ArrayList<>();
        for (Field field : fields) {
            if (field.getName().equalsIgnoreCase("serialVersionUID") || field.getName().equalsIgnoreCase("minOutIn") ) {
                continue;
            }
            String fieldName = field.getName();
            RiskMap riskMap = EntityUtil.copyNotNullFields(RiskConsts.RISK_MAP.get(fieldName), new RiskMap());
            try {
                field.setAccessible(true);
                riskMap.setAmount((Integer) field.get(riskAmount));
            } catch (IllegalAccessException e) {
                log.error("{}", e);
                throw new MyException(TipsConsts.SERVER_ERROR);
            }
            riskList.add(riskMap);
        }
        RiskMap outAndIn = RiskConsts.RISK_MAP.get("minOutIn");
        RiskMap riskMap = new RiskMap();
        riskMap.setCode(outAndIn.getCode());
        riskMap.setMsg(outAndIn.getMsg());
        Integer outAndInCount = getOutAndInCount(query);
        riskMap.setAmount(outAndInCount);
        riskList.add(riskMap);
        riskList.sort(Comparator.comparing(RiskMap::getAmount).reversed());
        return riskList;
    }

    private Integer getOutAndInCount(RiskByRankQuery query) {
        String tableName = "tietou_same_station_frequently";
        if (query.getIsCurrent().equals(YesNoEnum.NO.getCode())) {
            tableName = "highway_tietou.tietou_same_station_frequently";
        }
        query.setTableName(tableName);

        return tietouSameStationFrequentlyMapper.getSameStationCountByTimeAndDistance(query);
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
        RiskAmountDto cheatingCount = tietouMapper.getCheatingCount(carType);
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
//                int amount = i <= 201907 ? ThreadLocalRandom.current().nextInt(10) + 1 : 0;
                amountDtos.add(new PeriodAmountDto(key, 0));
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
        PageHelper.startPage(travelRecordQueryDto.getPageNum(), travelRecordQueryDto.getPageSize());
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
     *
     * @param isCurrent
     * @param carId
     * @return
     */
    public DiffCarNoStaticDto getDiffCarNoStatic(Integer carId, Integer isCurrent) {
        String tableName = "tietou";
        String extractionName = "tietou_feature_extraction";
        if (isCurrent.equals(YesNoEnum.NO.getCode())) {
            tableName = "highway_tietou.tietou_2019";
            extractionName = "highway_tietou.tietou_feature_extraction";
        }
        List<DiffCarNoEnvlpDto> diffCarNoEnvlpDtoList = tietouMapper.stasticDiffCarNoByCarId(carId, tableName, extractionName);
        List<DiffCarNoEnvlpDto> diffDtoList = new ArrayList<>(10);
        for (DiffCarNoEnvlpDto dto : diffCarNoEnvlpDtoList) {
            if (Levenshtein.calc(dto.getOutCarNo(), dto.getCarNo()) <= ConfigConsts.SAME_CAR_SCORE) {
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
            currentProxy.staticOutNumByEnvlp(dto, latch, tableName);
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
    public void staticOutNumByEnvlp(DiffCarNoEnvlpDto dto, CountDownLatch latch, String tableName) {
        Integer outNum = tietouMapper.staticOutNumByEnvlp(dto.getCarId(), tableName);
        dto.setOutNum(outNum);
        latch.countDown();
    }

    /**
     * 查询车辆的车型分布
     *  @param vlpId
     * @param flag  1为查询出站车牌，2为进站车牌
     * @param isCurrent
     */
    public List<PeriodAmountDto> listCarTypeDetail(Integer vlpId, Integer flag, Integer isCurrent) {
        return tietouMapper.listCarTypeDetail(vlpId, flag,isCurrent);
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
        //该高速路每个站点风险数据
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps(localVariableConfig.getRiskMapCacheKey());
        if (hashOperations.size() > 0) {
            List<Object> objectList = hashOperations.values();
            for (Object o : objectList) {
                StationRiskCountDto dto = new StationRiskCountDto();
                if (o instanceof StationRiskCountDto) {
                    dto = (StationRiskCountDto) o;
                } else {
                    LinkedHashMap<String, Object> linkedHashMap = (LinkedHashMap<String, Object>) o;
                    dto.setCkId((Integer) linkedHashMap.get("ckId"));
                    dto.setCkName((String) linkedHashMap.get("ckName"));
                    dto.setLongitude((String) linkedHashMap.get("longitude"));
                    dto.setLatitude((String) linkedHashMap.get("latitude"));
                    dto.setTotal((Integer) linkedHashMap.get("total"));
                    dto.setHigh((Integer) linkedHashMap.get("high"));
                    dto.setMiddle((Integer) linkedHashMap.get("middle"));
                    dto.setLow((Integer) linkedHashMap.get("low"));
                }
                riskCountDtoList.add(dto);
            }
            riskCountDtoList.sort(Comparator.comparing(StationRiskCountDto::getHigh).reversed().thenComparing(StationRiskCountDto::getTotal));
        } else {
            List<Integer> stationIdList = stationDicMapper.getCurrentStationId(localVariableConfig.getEnterpriseCode());
            if (CollectionUtils.isEmpty(stationIdList)) {
                return null;
            }
            riskCountDtoList = tietouMapper.statistic2ndStationRiskCount(stationIdList);
            List<StationRiskCountDto> stationDicList = stationDicMapper.list2ndStation(localVariableConfig.getEnterpriseCode());
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

            for (StationRiskCountDto stu : stationDicList) {
                for (StationRiskCountDto dto : riskCountDtoList) {
                    if (dto.getCkId().equals(stu.getCkId())) {
                        stu.setTotal(dto.getTotal());
                        stu.setHigh(dto.getHigh());
                        stu.setMiddle(dto.getMiddle());
                        stu.setLow(dto.getLow());
                        break;
                    }
                }
                if (stu.getTotal() == null) {
                    stu.setTotal(0);
                }
                if (stu.getHigh() == null) {
                    stu.setHigh(0);
                }
                if (stu.getMiddle() == null) {
                    stu.setMiddle(0);
                }
                if (stu.getLow() == null) {
                    stu.setLow(0);
                }
                map.put(String.valueOf(stu.getCkId()), stu);
            }

            hashOperations.putAll(map);
        }

        return riskCountDtoList;
    }

    /**
     * 查询速度过慢，且短途重载或长途轻载的记录
     * @param carNoId 车牌ID
     * @param limit 查询记录数
     * @param overWeightFlag
     * @param lightWeightFlag
     */
    public List<TietouOrigin> listLowSpeedAndWeight(Integer carNoId, int limit, boolean overWeightFlag, boolean lightWeightFlag) {
        return tietouMapper.listLowSpeedAndWeight(carNoId,limit,overWeightFlag,lightWeightFlag);
    }

    public List<TietouOrigin> listSameCarNumRecord(Integer carNoId, int limit) {
        return tietouMapper.listSameCarNumRecord(carNoId,limit);
    }

    /**
     * 根据出口车牌和分页pageSize获取当前数据id在该车牌行程记录的分页pageNum
     * @param dto
     * @return
     */
    public PageNumByCardIdResponse getPageNumByCarId(TravelRecordPageNumDto dto) {
        PageNumByCardIdResponse response = new PageNumByCardIdResponse();
        Integer tietou2019Id ;
        if (dto.getIsCurrent().equals(YesNoEnum.YES.getCode())) {
            tietou2019Id = tietou2019Mapper.getTietou2019IdByTietouId(dto.getRecordId());
        } else {
            tietou2019Id = dto.getRecordId();
        }
        response.setRecordId(tietou2019Id);
        List<Integer> idList = tietou2019Mapper.getIdListByCarId(dto.getCarId());
        int index = 1;
        for (int i = 0; i < idList.size(); i++) {
            Integer id = idList.get(i);
            if (tietou2019Id.equals(id)) {
                index = i + 1;
                break;
            }
        }
        int pageSize = dto.getPageSize();

        if (index % pageSize == 0) {

            response.setPageNum(index/pageSize);
        } else {
            response.setPageNum(index/pageSize + 1);
        }
        return response;
    }

    /**
     * 查询铁投表中小于当前时间的最大出站时间
     * @param now 当前时间
     */
    public LocalDate selectMaxTime(LocalDate now) {
        return tietouMapper.selectMaxTime(now);
    }

    /**
     * 根据id查询tietou_2019中的数据
     * @param idList
     * @return
     */
    public List<TietouOrigin> getTieTou2019ByIdList(List<Integer> idList) {
        List<TietouOrigin> originList = tietou2019Mapper.listAllByIdList(idList);
        return originList;
    }

    public CarDetailResponse getCarDetail(Integer carId) {
        TietouCarDic carDic = tietouCarDicMapper.selectById(carId);
        CarDetailResponse response = new CarDetailResponse();
        response = EntityUtil.copyNotNullFields(carDic, response);
        Integer travelCount = tietouMapper.getCountByVlpId(carId, null);
        Integer totalTravelCount = tietou2019Mapper.getCountByVlpId(carId);
        BigDecimal score = tietouFeatureStatisticMapper.getScoreByVlpId(carId);
        response.setTravelCount(travelCount);
        response.setTotalTravelCount(totalTravelCount);
        if (score != null) {
            response.setScore(score.setScale(2, BigDecimal.ROUND_HALF_UP));
        }
        return response;
    }

    /**
     * 拉取tietoudata
     * @param currentTietouId
     * @param allMaxTietouId
     * @param stationIdList
     */
    public void pullTietouDataFromAll(Integer currentTietouId, Integer allMaxTietouId, List<Integer> stationIdList) {
        long timeMillis = System.currentTimeMillis();
        Integer maxId = allMaxTietouId;
        Integer startId = 1;
        if (currentTietouId != null) {
            startId = currentTietouId;
        }
        int distance = 1000000;
        int dis = 100000;
        TietouService currentProxy = applicationContext.getBean(TietouService.class);
        for (int i = startId; i <= maxId ; i += distance) {
            int boundary = i + distance < maxId ? i + distance - 1 : maxId;
            List<Tietou2019> tietou2019List = tietou2019Mapper.listByIdPeriod(i, boundary);
            int total = tietou2019List.size();
            int latchSize = total % dis == 0 ? total / dis : total / dis + 1;
            CountDownLatch latch = new CountDownLatch(latchSize);
            for (int j = 0; j < total; j += dis) {
                int nextJ = j + dis;
                int end = nextJ < total ? nextJ : total;
                currentProxy.pullTietouDataFromAllAsync(tietou2019List.subList(j, end), tietouMapper, latch, stationIdList);
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                log.error("{}", e);
                Thread.currentThread().interrupt();
            } finally {
                log.info("分批从铁投总表拉取tietou表数据已执行完{}条记录", boundary);
            }
        }
        log.info("从铁投总表拉取tietou表已完成，耗时{}秒", (System.currentTimeMillis() - timeMillis) / 1000);
    }

    /**
     * 多线程拉取铁头数据
     * @param subList
     * @param tietouMapper
     * @param latch
     * @param stationIdList
     */
    @Async("taskExecutor")
    public void pullTietouDataFromAllAsync(List<Tietou2019> subList, TietouMapper tietouMapper, CountDownLatch latch, List<Integer> stationIdList) {
        try {
            List<Tietou2019> currentList = new ArrayList<>(1000);
            int count = 0;
            for (Tietou2019 tietou2019 : subList) {
                Integer rkId = tietou2019.getRkId();
                Integer ckId = tietou2019.getCkId();
                if (stationIdList.contains(rkId) || stationIdList.contains(ckId)) {
                    currentList.add(tietou2019);
                }
                count++;
                if (currentList.size() % 1000 == 0 || count == subList.size()) {
                    if (currentList.size() > 0) {
                        tietouMapper.insertBatchWithIdAndIgnore(currentList);
                        currentList.clear();
                    }
                }

            }

        } catch (Exception e) {
            log.error("分批从铁投总表拉取tietou表数据出错。", e);
        } finally {
            latch.countDown();
        }
    }

    /**
     *
     * @param travelRecordQueryDto
     * @return
     */
    public List<TietouOrigin> getTieTou2019ByTime(TravelRecordQueryDto travelRecordQueryDto) {
        if (travelRecordQueryDto.getPageNum() == null) {
            travelRecordQueryDto.setPageNum(1);
        }
        if (travelRecordQueryDto.getPageSize() == null) {
            travelRecordQueryDto.setPageSize(10);
        }
        PageHelper.startPage(travelRecordQueryDto.getPageNum(), travelRecordQueryDto.getPageSize());
        List<TietouOrigin> list = tietou2019Mapper.listByTime(travelRecordQueryDto);
        return list;
    }
}
