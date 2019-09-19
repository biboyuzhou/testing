package com.drcnet.highway.service.dataclean;

import com.drcnet.highway.common.BeanConvertUtil;
import com.drcnet.highway.config.LocalVariableConfig;
import com.drcnet.highway.constants.CacheKeyConsts;
import com.drcnet.highway.constants.ConfigConsts;
import com.drcnet.highway.dao.*;
import com.drcnet.highway.domain.StatisticCount;
import com.drcnet.highway.dto.CarNoDto;
import com.drcnet.highway.dto.response.CommonTypeCountDto;
import com.drcnet.highway.dto.response.StationRiskCountDto;
import com.drcnet.highway.dto.response.StationTripCountDto;
import com.drcnet.highway.entity.*;
import com.drcnet.highway.entity.dic.StationDic;
import com.drcnet.highway.entity.dic.TietouCarDic;
import com.drcnet.highway.enums.CarTypeEnum;
import com.drcnet.highway.service.TietouSameStationFrequentlyService;
import com.drcnet.highway.service.TietouStationDicService;
import com.drcnet.highway.service.dic.TietouCarDicService;
import com.drcnet.highway.util.EntityUtil;
import com.drcnet.highway.util.Levenshtein;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author: penghao
 * @CreateTime: 2019/6/10 9:53
 * @Description:
 */
@Service
@Slf4j
public class TietouCleanService {
    @Resource
    private TietouMapper tietouMapper;
    @Resource
    private StationDicMapper stationDicMapper;
    @Resource
    private TietouCarDicMapper carDicMapper;
    @Resource
    private TietouCarDicService tietouCarDicService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private TietouFeatureStatisticMonthMapper tietouFeatureStatisticMonthMapper;
    @Resource
    private TietouFeatureExtractionMapper tietouFeatureExtractionMapper;
    @Resource
    private TietouFeatureExtractionMapper extractionMapper;
    @Resource
    private TietouSameStationFrequentlyService tietouSameStationFrequentlyService;
    @Resource
    private TietouFeatureStatisticGyhMapper tietouFeatureStatisticGyhMapper;
    @Resource
    private TietouFeatureStatisticMapper tietouFeatureStatisticMapper;
    @Resource
    private StationFeatureStatisticsMapper stationFeatureStatisticsMapper;
    @Resource
    private TaskExecutor taskExecutor;
    @Resource
    private LocalVariableConfig localVariableConfig;

    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private Tietou2019Mapper tietou2019Mapper;

    @Resource
    private TietouStationDicService tietouStationDicService;

    private Map<String, Integer> stationMap;

    @PostConstruct
    public void initCache() {
        List<StationDic> stationDics = stationDicMapper.selectAllStation();
        stationMap = new ConcurrentHashMap<>(stationDics.stream().collect(Collectors.toMap(StationDic::getStationName, StationDic::getId)));
    }

    /**
     * 修改外键id，包括 rk_id,ck_id,envlp_id,vlp_id
     */
    public void updateForeignId() {
        long timeMillis = System.currentTimeMillis();
        TietouCleanService currentProxy = (TietouCleanService) AopContext.currentProxy();
        Integer maxId = tietouMapper.selectCurrentMaxId();
        int distance = 500000;
        int dis = 10000;
        for (int i = 0; i <= maxId; i += distance) {
            List<TietouOrigin> tietous = tietouMapper.listByIdPeriod(i, i + distance);
            int size = tietous.size();
            int latchSize = size % dis == 0 ? size / dis : size / dis + 1;
            CountDownLatch latch = new CountDownLatch(latchSize);
            for (int j = 0; j < size; j += dis) {
                int nextJ = j + dis;
                int boundary = nextJ < size ? nextJ : size;
                currentProxy.updateIds(tietous.subList(j, boundary), latch, currentProxy);
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                log.error("{}", e);
                Thread.currentThread().interrupt();
            } finally {
                log.info("已执行完{}条记录", i + distance - 1);
            }
        }
        log.info("所有记录已更新完成，耗时{}秒", (System.currentTimeMillis() - timeMillis) / 1000);

    }

    @Transactional
    public void updateIds(List<TietouOrigin> tietouList, CountDownLatch latch) {
        for (TietouOrigin tietou : tietouList) {
            String envlp = tietou.getEnvlp();
            String vlp = tietou.getVlp();
            if (envlp != null) {
                CarNoDto envlpClean = tietouCarDicService.getOrInsertByCarNo(envlp, tietou.getEnvc());
                tietou.setEnvlpId(envlpClean.getId());
                tietou.setEnvlp(envlpClean.getCarNo());
            }
            if (vlp != null) {
                CarNoDto vlpClean = tietouCarDicService.getOrInsertByCarNo(vlp, tietou.getVc());
                tietou.setVlpId(vlpClean.getId());
                tietou.setVlp(vlpClean.getCarNo());
            }
            Integer rkId = tietouStationDicService.getOrInertByName(tietou.getRk());
            Integer ckId = tietouStationDicService.getOrInertByName(tietou.getCk());
            tietou.setRkId(rkId);
            tietou.setCkId(ckId);
            tietouMapper.updateByPrimaryKeyAction(tietou);
        }
        latch.countDown();
    }

    @Async("taskExecutor")
    public void updateIds(List<TietouOrigin> tietouList, CountDownLatch latch, TietouCleanService currentProxy) {
        currentProxy.updateIds(tietouList, latch);
    }


    /**
     * 将车牌信息以 车牌号-车牌id的方式存入redis
     */
    public void initCarDic2Cache() {
        String cacheName = "car_cache";
        Integer maxId = carDicMapper.getMaxId();
        int distance = 50000;
        redisTemplate.delete(cacheName);
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps(cacheName);
        for (int i = 1; i <= maxId; i += distance) {
            List<TietouCarDic> carDics = carDicMapper.selectByPeriod(i, i + distance);
            Map<String, Integer> collect = carDics.stream().collect(Collectors.toMap(TietouCarDic::getCarNo, TietouCarDic::getId));
            hashOperations.putAll(collect);
            int latchSize = i + distance - 1 < maxId ? i + distance - 1 : maxId;
            log.info("已导入{}条数据", latchSize);
        }
        log.info("数据导入redis成功！");
    }

    /**
     * 将use_flag=0的车牌以车牌ID-车牌号的方式存入redis中
     */
    public void initUseFlagFalseCarDic2Cache(Integer start) {
        redisTemplate.delete(CacheKeyConsts.CAR_CACHE_USELESS);
        BoundHashOperations<String, String, String> hashOperations = redisTemplate.boundHashOps(CacheKeyConsts.CAR_CACHE_USELESS);
        Integer maxId = carDicMapper.getMaxId();
        int distance = 50000;
        for (int i = start; i <= maxId; i += distance) {
            int end = (i + distance - 1) >= maxId ? maxId : (i + distance - 1);
            List<TietouCarDic> res = carDicMapper.selectUselessCarByPeriod(i, end);
            Map<String, String> uselessCar = res.stream().collect(Collectors.toMap(var -> var.getId().toString(), TietouCarDic::getCarNo));
            hashOperations.putAll(uselessCar);
            uselessCar.clear();
            log.info("useFlag false数据已导入redis{}条！", end);
        }
    }

    /**
     * 将出现次数最多的轴数和车型设置为车牌的轴数
     */
    public void statisticsAlexNumAndCarType(Integer start, Integer end) {
        if (end == null) {
            end = carDicMapper.getMaxId();
        }
        int distance = 10000;
        TietouCleanService currentProxy = (TietouCleanService) AopContext.currentProxy();
        int total = end - start;
        int latchCount = total % distance == 0 ? total / distance : total / distance + 1;
        CountDownLatch latch = new CountDownLatch(latchCount);
        for (int i = start; i <= end; i += distance) {
            currentProxy.statisticsAlexNum(i, distance, latch);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("{}", e);
            Thread.currentThread().interrupt();
        }
        log.info("轴数和车型数据导入字典表成功！");
    }

    @Transactional(rollbackFor = Exception.class)
    @Async("taskExecutor")
    public void statisticsAlexNum(int begin, int distance, CountDownLatch latch) {
        List<TietouCarDic> tietouCarDics = carDicMapper.selectByPeriod(begin, begin + distance - 1);
        for (TietouCarDic carDic : tietouCarDics) {
            Integer vlpId = carDic.getId();
            Integer carTypeDic = carDic.getCarType();
            Integer carTypeDicIn = carDic.getCarTypeIn();
            Integer axlenumDic = carDic.getAxlenum();
            Integer axlenumInDic = carDic.getAxlenumIn();
            Integer axleNum = tietouMapper.getMostUseAxleNum(vlpId, 1, null);
            Integer carType = tietouMapper.getMostUseCarType(vlpId, 1, null);
            Integer axleNumIn = tietouMapper.getMostUseAxleNum(vlpId, 0, null);
            Integer carTypeIn = tietouMapper.getMostUseCarType(vlpId, 0, null);
            if (!ObjectUtils.nullSafeEquals(axlenumDic, axleNum) || !ObjectUtils.nullSafeEquals(axlenumInDic, axleNumIn)
                    || !ObjectUtils.nullSafeEquals(carTypeDic, carType) || !ObjectUtils.nullSafeEquals(carTypeDicIn, carTypeIn)) {
                carDic.setAxlenum(axleNum);
                carDic.setCarType(carType);
                carDic.setAxlenumIn(axleNumIn);
                carDic.setCarTypeIn(carTypeIn);
                carDicMapper.updateAxlenumById(carDic);
            }
        }
        latch.countDown();
        log.info("{}-{} 区间数据已统计完成，剩余数量:{}", begin, begin + distance - 1, latch.getCount() * 10000);
    }

    /**
     * 按月统计次数
     *
     * @param monthTime 月度时间
     */
    public void statisticFeatureAmountByMonth(Integer monthTime) {
        BoundHashOperations<String, String, String> hashOperations = redisTemplate.boundHashOps("car_cache_useless");
        Set<String> keys = hashOperations.keys();
        TietouCleanService currentProxy = (TietouCleanService) AopContext.currentProxy();
        List<TietouFeatureStatisticMonth> statisticMonths = tietouFeatureStatisticMonthMapper.statisticFeatureAmountByMonth(monthTime);
        long timeMillis = System.currentTimeMillis();
        log.info("开始统计月份:{} 的违规特征数据", monthTime);
        int size = statisticMonths.size();
        int distance = 10000;
        int latchCount = size % distance == 0 ? size / distance : size / distance + 1;
        CountDownLatch latch = new CountDownLatch(latchCount);
        for (int i = 0; i < statisticMonths.size(); i += distance) {
            int scope = i + distance < size ? i + distance : size;
            currentProxy.statisticFeatureAmountByMonthAction(monthTime, statisticMonths.subList(i, scope), latch, keys);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("{}", e);
            Thread.currentThread().interrupt();
        }
        log.info("{}，月度违规次数统计成功！耗时:{} 秒", monthTime, (System.currentTimeMillis() - timeMillis) / 1000);
    }

    /**
     * 将所有月份的违规数量按车牌统计到一起
     *
     * @param monthTime
     * @param dataMap
     */
    public void statisticsAllAmountFrom(int monthTime, Map<Integer, TietouFeatureStatistic> dataMap) {
        TietouCleanService currentProxy = (TietouCleanService) AopContext.currentProxy();
        List<TietouFeatureStatistic> statistics = tietouFeatureStatisticMapper.listByMonthTime(monthTime);
        log.info("查询出{}的数据,开始进行封装", monthTime);

        if (CollectionUtils.isEmpty(statistics)) {
            return;
        }
        if (dataMap.isEmpty()) {
            Map<Integer, TietouFeatureStatistic> statisticMap = statistics.parallelStream().collect(Collectors.toMap(TietouFeatureStatistic::getVlpId, var -> var));
            dataMap.putAll(statisticMap);
        } else {
            CountDownLatch latch = new CountDownLatch(statistics.size());
            for (TietouFeatureStatistic statistic : statistics) {
                currentProxy.sumAmount2Vlp(dataMap, statistic, latch);
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                log.error("{}", e);
                Thread.currentThread().interrupt();
            }
        }
        log.info("{}的数据,已封装完成", monthTime);
    }

    /**
     * 将封装好的结果新增到数据库
     *
     * @param dataMap
     */
    public void insertAmountStatistics2DB(Map<Integer, TietouFeatureStatistic> dataMap) {
        TietouCleanService currentProxy = (TietouCleanService) AopContext.currentProxy();
        List<TietouFeatureStatistic> dataList = dataMap.values().parallelStream()
                .sorted(Comparator.comparing(TietouFeatureStatistic::getTotal).reversed()).collect(Collectors.toList());
        int distance = 10000;
        int latchSize = dataList.size() % distance == 0 ? dataList.size() / distance : dataList.size() / distance + 1;
        CountDownLatch latch = new CountDownLatch(latchSize);
        for (int i = 0; i < dataList.size(); i += distance) {
            int boundary = i + distance < dataList.size() ? i + distance : dataList.size();
            List<TietouFeatureStatistic> featureStatistics = dataList.subList(i, boundary);
            currentProxy.insertAllStatistics(featureStatistics, latch);
            if (i % 200000 == 0 || boundary == dataList.size()) {
                log.info("正在处理第:{}条数据", i);
            }
        }
        try {
            log.info("statistic 数据全部入库成功！");
            latch.await();
        } catch (InterruptedException e) {
            log.error("{}", e);
            Thread.currentThread().interrupt();
        }

    }

    @Async("taskExecutor")
    @Transactional
    public void insertAllStatistics(List<TietouFeatureStatistic> featureStatistics, CountDownLatch latch) {
        tietouFeatureStatisticMapper.insertList(featureStatistics);
        latch.countDown();
    }

    @Async("taskExecutor")
    public void sumAmount2Vlp(Map<Integer, TietouFeatureStatistic> dataMap, TietouFeatureStatistic statistic, CountDownLatch latch) {
        Integer vlpId = statistic.getVlpId();
        TietouFeatureStatistic oldStatistics = dataMap.get(vlpId);
        if (oldStatistics == null) {
            dataMap.put(vlpId, statistic);
        } else {
            oldStatistics.sumFeatures(statistic);
        }
        latch.countDown();
        long latchCount = latch.getCount();
        if (latchCount % 100000 == 0) {
            log.info("当年剩余数量:{}", latchCount);
        }
    }

    @Async("taskExecutor")
    @Transactional
    public void statisticFeatureAmountByMonthAction(Integer monthTime, List<TietouFeatureStatisticMonth> statisticMonths
            , CountDownLatch latch, Set<String> keys) {

        for (TietouFeatureStatisticMonth statisticMonth : statisticMonths) {
            Integer vlpId = statisticMonth.getVlpId();
            TietouCarDic tietouCarDic = carDicMapper.selectById(vlpId);
            Integer carType = tietouCarDic.getCarType();
            if (carType != null) {
                if (carType >= 10) {
                    statisticMonth.setCarType(CarTypeEnum.TRUCKS.code);
                } else if (carType > 0) {
                    statisticMonth.setCarType(CarTypeEnum.COACH.code);
                } else {
                    statisticMonth.setCarType(CarTypeEnum.UNKNOWN.code);
                }
            }
            //在黑名单内不新增
            if (keys.contains(String.valueOf(vlpId))) {
                continue;
            }
            statisticMonth.setMonthTime(monthTime);
            tietouFeatureStatisticMonthMapper.insertSelective(statisticMonth);
        }
        latch.countDown();
        log.info("剩余记录数:{}", latch.getCount() * 10000);

    }

    @Async("taskExecutor")
    public Future<TietouCarDic> testAsyncReturn() {
        TietouCarDic carDic = new TietouCarDic();
        carDic.setCarType(1);
        carDic.setAxlenum(2);
        return new AsyncResult<>(carDic);
    }

    /**
     * 修正路段标志
     */
    public void amendStationFlag() {
        /*BoundSetOperations<String, Object> flagLost = redisTemplate.boundSetOps("flagLost");
        Set<Object> flagLostMembers = flagLost.members();
        Integer affect = tietouMapper.updateFlagLostExtractionTrue(flagLostMembers);
        log.info("修改行数:{}",affect);*/
        TietouCleanService currentProxy = (TietouCleanService) AopContext.currentProxy();
        BoundSetOperations<String, Object> flagLost = redisTemplate.boundSetOps("flagDiff");
        Set<Object> flagDiffMembers = flagLost.members();
        Integer[] arrays = flagDiffMembers.toArray(new Integer[0]);
        int distance = 10000;
        int count = arrays.length % distance == 0 ? arrays.length / distance : arrays.length / distance + 1;
        CountDownLatch latch = new CountDownLatch(count);
        for (int i = 0; i < arrays.length; i += distance) {
            currentProxy.updateStationFlagDiffTrue(arrays, i, distance, latch);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("{}", e);
            Thread.currentThread().interrupt();
        }

    }

    @Transactional
    @Async("taskExecutor")
    public void updateStationFlagDiffTrue(Integer[] arrays, int index, int distance, CountDownLatch latch) {
        TietouFeatureExtraction extraction = new TietouFeatureExtraction();
        //0表示不标记
        extraction.setDiffFlagstationInfo(0);
        int len = index + distance < arrays.length ? index + distance : arrays.length;
        for (int i = index; i < len; i++) {
            extraction.setId(arrays[i]);
            tietouFeatureExtractionMapper.updateByPrimaryKeySelective(extraction);
        }
        latch.countDown();
        log.info("剩余需要更新的记录数:{}", latch.getCount() * distance);
    }


    /**
     * 根据通行记录原始数据，得出12项特征的flag
     */
    public void insertExtractionData(Integer start, Integer maxId) {
        BoundHashOperations<String, String, String> hashOperations = redisTemplate.boundHashOps("car_cache_useless");
        long timeMillis = System.currentTimeMillis();
        TietouCleanService currentProxy = (TietouCleanService) AopContext.currentProxy();
        if (maxId == null) {
            maxId = tietouMapper.selectCurrentMaxId();
        }

        int distance = 300000;
        int dis = 500;
        for (int i = start; i <= maxId; i += distance) {
            int end = (i + distance - 1) >= maxId ? maxId : (i + distance - 1);
            List<TietouOrigin> tietous = tietouMapper.listAllByIdPeriod(i, end);
            log.info("已查询完{}条记录", end);
            int size = tietous.size();
            int latchSize = size % dis == 0 ? size / dis : size / dis + 1;
            CountDownLatch latch = new CountDownLatch(latchSize);
            for (int j = 0; j < size; j += dis) {
                int nextJ = j + dis;
                int boundary = nextJ < size ? nextJ : size;
                currentProxy.extractionFlagAndInsert(tietous.subList(j, boundary), latch, hashOperations);
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                log.error("{}", e);
                Thread.currentThread().interrupt();
            } finally {
                log.info("已执行完{}条记录", i + distance - 1);
            }
        }
        log.info("所有记录已新增完成，耗时{}秒", (System.currentTimeMillis() - timeMillis) / 1000);
    }

    /**
     * 统计车牌在同一时间有重复路径的记录，并保存标记
     */
    public void statisticSameTimeRange(Integer beginMonth) {
        TietouCleanService dataCleanService = (TietouCleanService) AopContext.currentProxy();
        //只查询2019年1月1日后进站的数据
        LocalDateTime firstTime = LocalDateTime.of(2019, 1, 1, 0, 0);
        List<TietouOrigin> tietouOrigins = tietouMapper.selectAllTimeByMonth(beginMonth, firstTime);
        long timeMillis = System.currentTimeMillis();
        log.info("开始封装map，月份:{}", beginMonth);
        Map<Integer, List<TietouOrigin>> tietouMap = record2MapByVlpId(tietouOrigins);
        log.info("map封装成功，耗时{}", (System.currentTimeMillis() - timeMillis) / 1000);
        BoundHashOperations<String, String, String> hashOperations = redisTemplate.boundHashOps("car_cache_useless");
        CountDownLatch latch = new CountDownLatch(tietouMap.size());
        int i = 0;
        for (Map.Entry<Integer, List<TietouOrigin>> entry : tietouMap.entrySet()) {
            Integer vlpId = entry.getKey();
            List<TietouOrigin> value = entry.getValue();
            dataCleanService.statisticSameTimeRangeByMap(vlpId, value, beginMonth, latch, hashOperations, dataCleanService);
            if (++i % 200000 == 0) {
                log.info("已执行完:{} 辆车", i);
            }
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        log.info("月份:{},处理完成", beginMonth);
    }

    /**
     * 将原始记录以vlpid分组，对每组内的数据进行统计
     *
     * @param origins
     * @param latch
     */
    @Async("taskExecutor")
    public void statisticSameTimeRangeByMap(Integer vlpId, List<TietouOrigin> origins, Integer monthTime, CountDownLatch latch
            , BoundHashOperations<String, String, String> hashOperations, TietouCleanService dataCleanService) {
        if (origins.size() == 1 || hashOperations.hasKey(String.valueOf(vlpId))) {
            latch.countDown();
            return;
        }
        //有重复驶入记录的id列表
        Set<TietouOrigin> markedData = new HashSet<>();
        //将记录分组
        List<Set<Integer>> groupSetList = new ArrayList<>();
        for (TietouOrigin father : origins) {
            Integer fid = father.getId();
            LocalDateTime fEntime = father.getEntime();
            LocalDateTime fExtime = father.getExtime();
            Integer fCkId = father.getCkId();
            Integer fRkId = father.getRkId();
            TreeSet<Integer> treeSet = null;
            for (TietouOrigin child : origins) {
                if (father.getId().equals(child.getId())) {
                    continue;
                }
                Integer cid = child.getId();
                LocalDateTime cEntime = child.getEntime();
                LocalDateTime cExtime = child.getExtime();
                Integer cCkId = child.getCkId();
                Integer cRkId = child.getRkId();
                //判断是否时间重叠,1、进站时间不一致、出站时间在30分钟以内不算时间重叠
                if (cEntime.isBefore(cExtime)
                        && (cEntime.isAfter(fEntime) && cEntime.isBefore(fExtime) || cExtime.isAfter(fEntime) && cExtime.isBefore(fExtime))) {
                    //出站时间差值，单位秒
                    long exitDistance = Duration.between(cExtime, fExtime).getSeconds();
                    //father出站和child进站时间差值
                    long inAndExitDistance = Duration.between(cEntime, fExtime).getSeconds();
                    //进站相同并且出站时间在30分钟以内，则不算时间重叠，反之则算
                    if (!(ObjectUtils.nullSafeEquals(fCkId, cCkId) && ObjectUtils.nullSafeEquals(fRkId, cRkId)
                            && cEntime.equals(fEntime) && Math.abs(exitDistance) < ConfigConsts.MAX_TIME_DIS
                            || ObjectUtils.nullSafeEquals(fCkId, cRkId) && inAndExitDistance < ConfigConsts.MAX_IN_AND_EXIT_DIS)) {
                        markedData.add(child);
                        markedData.add(father);
                        if (treeSet == null) {
                            treeSet = new TreeSet<>();
                        }
                        treeSet.add(fid);
                        treeSet.add(cid);
                    }
                }
            }
            if (!CollectionUtils.isEmpty(treeSet)) {
                groupSetList.add(treeSet);
            }
        }
        if (!markedData.isEmpty()) {
            //获得分组的列表,并去除重复和被包含的元素例如 当List内有 1,2,3和1,2的成员，1,2的成员将被删除
            Iterator<Set<Integer>> iterator = groupSetList.iterator();
            while (iterator.hasNext()) {
                Set<Integer> next = iterator.next();
                for (Set<Integer> re : groupSetList) {
                    if (!re.equals(next) && re.containsAll(next)) {
                        iterator.remove();
                        break;
                    }
                }
            }
            //去重
            groupSetList = groupSetList.stream().distinct().collect(Collectors.toList());

            Map<Integer, Set<Integer>> allGroupMap = new LinkedHashMap<>();
            for (int i = 0; i < groupSetList.size(); i++) {
                allGroupMap.put(i + 1, groupSetList.get(i));
            }
            //判断当当前记录的ID被包含在分组内时，生成分组标记
            dataCleanService.updateSameTimeRangeAgan(markedData, allGroupMap, monthTime);
//            TietouOrigin tietouOrigin = origins.get(0);
//            log.info("vlpID:{},有重复驶入记录，已更新至数据库", tietouOrigin.getVlpId());
        }
        latch.countDown();
    }

    @Transactional
    public void updateSameTimeRangeAgan(Set<TietouOrigin> markedData, Map<Integer, Set<Integer>> allGroupMap, Integer monthTime) {
        for (TietouOrigin tietouOrigin : markedData) {
            StringBuilder builder = new StringBuilder();
            Integer id = tietouOrigin.getId();
            for (Map.Entry<Integer, Set<Integer>> entry : allGroupMap.entrySet()) {
                Integer key = entry.getKey();
                Set<Integer> value = entry.getValue();
                if (value.contains(id)) {
                    if (builder.length() != 0) {
                        builder.append(",");
                    }
                    builder.append(monthTime + "-" + key);
                }
            }
            if (builder.length() > 190) {
                builder = new StringBuilder();
            }
            //同时更新标记和分组
            tietouFeatureExtractionMapper.updateSameRouteMarkAndLabel(tietouOrigin.getId(), builder.toString());
        }
    }

    //将列表重组为map
    private Map<Integer, List<TietouOrigin>> record2MapByVlpId(List<TietouOrigin> tietouOrigins) {
        Map<Integer, List<TietouOrigin>> map = new HashMap<>(1 << 5);
        for (TietouOrigin tietouOrigin : tietouOrigins) {
            Integer vlpId = tietouOrigin.getVlpId();
            if (vlpId == null) {
                continue;
            }
            List<TietouOrigin> tietous = map.get(vlpId);
            if (tietous != null) {
                tietous.add(tietouOrigin);
            } else {
                tietous = new ArrayList<>(32);
                tietous.add(tietouOrigin);
                map.put(vlpId, tietous);
            }
        }
        return map;
    }

    /**
     * 5分钟内先出后进统计
     *
     * @param monthTime
     */
    public void calcOutAndInByJava(int monthTime) {
        boolean hasMonthTime = tietouSameStationFrequentlyService.hasMonthTime(monthTime);
        if (hasMonthTime) {
            log.info("{},月份的数据已存在，不需要进行统计", monthTime);
            return;
        }
        TietouCleanService dataCleanService = (TietouCleanService) AopContext.currentProxy();
        //只查询2019年1月1日后进站的数据
        LocalDateTime firstTime = LocalDateTime.of(2019, 1, 1, 0, 0);
        List<TietouOrigin> tietouOrigins = tietouMapper.selectAllTimeByMonth(monthTime, firstTime);
        long timeMillis = System.currentTimeMillis();
        log.info("开始封装map，月份:{}", monthTime);
        Map<Integer, List<TietouOrigin>> tietouMap = record2MapByVlpId(tietouOrigins);
        log.info("map封装成功,共{}辆车，耗时{}", tietouMap.size(), (System.currentTimeMillis() - timeMillis) / 1000);
        BoundHashOperations<String, String, String> uselessCarOperations = redisTemplate.boundHashOps(CacheKeyConsts.CAR_CACHE_USELESS);
        CountDownLatch latch = new CountDownLatch(tietouMap.size());
        Set<Integer> idUpdateSets = Collections.newSetFromMap(new ConcurrentHashMap<>(2048));
        int i = 0;
        for (Map.Entry<Integer, List<TietouOrigin>> entry : tietouMap.entrySet()) {
            Integer vlpId = entry.getKey();
            List<TietouOrigin> value = entry.getValue();
            dataCleanService.minOutInStatisticsAction(vlpId, value, monthTime, latch, uselessCarOperations, idUpdateSets, dataCleanService);
            if (++i % 200000 == 0) {
                log.info("已处理:{}辆车", i);
            }
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        log.info("月份:{},处理完成", monthTime);
    }

    /**
     * 5分钟内先出后进
     */
    @Async("taskExecutor")
    public void minOutInStatisticsAction(Integer vlpId, List<TietouOrigin> origins, int monthTime, CountDownLatch latch
            , BoundHashOperations<String, String, String> uselessCarOperations, Set<Integer> idUpdateSets, TietouCleanService dataCleanService) {
        if (origins.size() == 1 || uselessCarOperations.hasKey(String.valueOf(vlpId))) {
            latch.countDown();
            return;
        }
        Set<Integer> idSet = new HashSet<>();
        Set<TietouSameStationFrequently> newVal = new HashSet<>();
        Set<TietouSameStationFrequently> oldVal = new HashSet<>();
        for (TietouOrigin father : origins) {
            Integer fid = father.getId();
            Integer fckId = father.getCkId();

            LocalDateTime fExtime = father.getExtime();
            //定义与当前记录达成先出后进的数据
            TietouOrigin next = null;
            Integer dis = null;
            for (TietouOrigin child : origins) {
                Integer crkId = child.getRkId();
                if (fckId == null || crkId == null
                        || !ObjectUtils.nullSafeEquals(fckId, crkId)
                        || !ObjectUtils.nullSafeEquals(father.getVlpId(), child.getEnvlpId())
                        || father.getId().equals(child.getId())) {
                    continue;
                }
                LocalDateTime cEntime = child.getEntime();
                //进站时间和出站时间短于300秒，并且next为空，或进站时间早于next进站时间，设置next为当前记录
                Long distance = Duration.between(fExtime, cEntime).getSeconds();
                if (distance <= 300 && distance > 0 && (next == null || cEntime.isBefore(next.getEntime()))) {
                    next = child;
                    dis = distance.intValue();
                }
            }
            if (next != null) {
                //将id放入集合一并更新
                idSet.add(fid);
                idSet.add(next.getId());
                Integer outId = father.getId();
                Integer inId = next.getId();
                TietouSameStationFrequently frequently = new TietouSameStationFrequently();
                frequently.setOutId(outId);
                frequently.setInId(inId);
                TietouSameStationFrequently res = tietouSameStationFrequentlyService.selectOne(frequently);
                frequently.setVlpId(father.getVlpId());
                frequently.setVlp(father.getVlp());
                frequently.setMonthTime(monthTime);
                frequently.setIntervalTime(dis);
                frequently.setStationId(father.getCkId());
                frequently.setTollStationName(father.getCk());
                frequently.setOutTime(father.getExtime());
                frequently.setInTime(next.getEntime());
                tietouSameStationFrequentlyService.setExtField(frequently, outId, inId);
                if (res == null) {
                    newVal.add(frequently);
                } else {
                    frequently.setId(res.getId());
                    oldVal.add(frequently);
                }
            }
        }
        if (!idSet.isEmpty()) {
            Set<Integer> ids = new HashSet<>();
            for (Integer id : idSet) {
                if (!idUpdateSets.contains(id)) {
                    idUpdateSets.add(id);
                    ids.add(id);
                }
            }
            if (!ids.isEmpty()) {
                //将集合内的id标记为先出后进
                dataCleanService.updateMinOutFlag(ids, newVal, oldVal);
            }
        }
        latch.countDown();

    }


    @Transactional
    public void updateMinOutFlag(Collection<Integer> ids, Set<TietouSameStationFrequently> newVal, Set<TietouSameStationFrequently> oldVal) {
        for (Integer id : ids) {
            tietouFeatureExtractionMapper.updateSameCarOutInFlagOne(id);
        }
        for (TietouSameStationFrequently frequently : newVal) {
            tietouSameStationFrequentlyService.insert(frequently);
        }
        for (TietouSameStationFrequently frequently : oldVal) {
            tietouSameStationFrequentlyService.updateByPrimaryKey(frequently);
        }
    }

    @Async("taskExecutor")
    public void extractionFlagAndInsert(List<TietouOrigin> subList, CountDownLatch latch, BoundHashOperations<String, String, String> hashOperations) {
        for (TietouOrigin tietou : subList) {
            Integer vlpId = tietou.getVlpId();
            TietouCarDic carDic = carDicMapper.selectById(vlpId);
            if (carDic == null) {
                if (vlpId != null) {
                    log.error("------{}无法查询到该车辆信息！", vlpId);
                }
                continue;
            }
            TietouFeatureExtraction extraction = new TietouFeatureExtraction();
            extraction.setId(tietou.getId());
            extraction.setMonthTime(tietou.getMonthTime());
            extraction.setVlpId(tietou.getVlpId());
            extraction.setVlp(tietou.getVlp());
            extraction.setRkId(tietou.getRkId());
            extraction.setCkId(tietou.getCkId());
            //先出后进和通行时间重叠需单独统计
            extraction.setMinOutIn(0);
            extraction.setSameTimeRangeAgain(0);
            //判断车牌不一致
            sameCarNumJudgement(tietou, extraction, hashOperations);
            //判断车型不一致
            sameCarTypeJudgement(tietou, extraction);
            //判断车情是否一致
            sameCarSituationJudgement(tietou, extraction);
            //判断是否是同站进出
            sameStationJudgement(tietou, extraction);
            //判断标志
            stationFlagJudgement(tietou, extraction);
            //判断speed
            speedJudgement(tietou, extraction);
            //判断载重
            weightLimitJudgement(tietou, extraction, carDic);
            //设置车型
            carTypeJudgement(tietou, extraction);
            //判断轴数不一致和客车货车类型
            axlenumJudgement(tietou, extraction, carDic);
            try {
                extractionMapper.insertSelective(extraction);
            } catch (Exception e) {
                log.error("查询extraction表出错", e);
            }

        }
        latch.countDown();
    }

    /**
     * 在归一化后的得分表内给从二绕经过的车辆做标记
     */
    @Transactional
    public void mark2ndRoundCars() {
        log.info("开始标记二绕车辆");
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps("second_round_car_no_ka");
        Set<Object> keys = hashOperations.keys();
        int affectAmount = tietouFeatureStatisticGyhMapper.updateSecondMarkByVlpIds(keys);
        log.info("共标记{}辆车", affectAmount);
    }

    private void carTypeJudgement(TietouOrigin tietou, TietouFeatureExtraction extraction) {
        //从当前记录拿车型
        Integer carType = tietou.getVc();
        if (carType == null) {
            extraction.setCarType(CarTypeEnum.UNKNOWN.code);
            return;
        }
        if (carType > 0 && carType < 10) {
            extraction.setCarType(CarTypeEnum.COACH.code);
        } else if (carType > 10) {
            extraction.setCarType(CarTypeEnum.TRUCKS.code);
        } else {
            extraction.setCarType(CarTypeEnum.UNKNOWN.code);
        }
    }

    private void axlenumJudgement(TietouOrigin tietou, TietouFeatureExtraction extraction, TietouCarDic carDic) {
        Integer axlenum = tietou.getAxlenum();
        Integer dicAxlenum = carDic.getAxlenum();
        if (dicAxlenum != null && axlenum != 0 && axlenum < 10 && !dicAxlenum.equals(axlenum)) {
            extraction.setDifferentZhou(1);
        } else {
            extraction.setDifferentZhou(0);
        }
        /*BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps("car_axlenum_cache");
        Integer vlpId = tietou.getVlpId();
        Integer axlenum = tietou.getAxlenum();
        TietouCarDic carDic = carDicMapper.selectByPrimaryKey(vlpId);
        if (carDic == null) {
            extraction.setDifferentZhou(0);
            return;
        }

        Integer dicAxlenum = carDic.getAxlenum();
        if (dicAxlenum != null && axlenum != 0 && !dicAxlenum.equals(tietou.getAxlenum())) {
            //如果与车辆轴数不一致
            String carnoId = String.valueOf(vlpId);
            if (hashOperations.get(carnoId) == null) {
                List<HashMap<String, Integer>> result = tietouMapper.countAxlenumByVlpId(vlpId);
                Integer count = 0;
                for (HashMap<String, Integer> map: result) {
                    Integer zhoushu = map.get("axlenum");
                    hashOperations.put(getCacheKey(carnoId, zhoushu), map.get("count"));
                    count += Integer.parseInt(String.valueOf(map.get("count")));
                }
                hashOperations.put(carnoId, count);
            }
            Integer total = (Integer) hashOperations.get(carnoId);
            Integer current = (Integer) hashOperations.get(getCacheKey(carnoId, axlenum));
            // 不一致的轴数占比大于20%才算异常
            if (current * 5 > total) {
                extraction.setDifferentZhou(1);
            } else {
                extraction.setDifferentZhou(0);
            }

        } else {
            extraction.setDifferentZhou(0);
        }*/

    }

    private String getCacheKey(String carnoId, Integer zhoushu) {
        return new StringBuilder(carnoId).append("_").append(zhoushu).toString();
    }

    /**
     * 统计短途重载和长度轻载
     * 总量大于最大重量80%路程小于30KM为短途重载，总量小于最小重量的120%路程大于100km为长途轻载
     */
    private void weightLimitJudgement(TietouOrigin tietou, TietouFeatureExtraction extraction, TietouCarDic carDic) {
        Integer weightMax = carDic.getWeightMax();
        Integer weightMin = carDic.getWeightMin();
        //如果理论最低载重和最大载重为null或者二者相等
        if (weightMax == null || weightMin == null || weightMax.equals(weightMin)) {
            extraction.setLongDisLightweight(0);
            extraction.setShortDisOverweight(0);
            return;
        }
        //最小载重如果大于最大载重的20%，则不算异常
        BigDecimal rate = new BigDecimal(weightMin).divide(new BigDecimal(weightMax), 1, BigDecimal.ROUND_HALF_UP);
        if (rate.compareTo(new BigDecimal(0.2)) > 0) {
            extraction.setLongDisLightweight(0);
            extraction.setShortDisOverweight(0);
            return;
        }
        //总里程
        Integer tolldistance = tietou.getTolldistance();
        //获得总重量
        Integer totalweight = tietou.getTotalweight();

        Integer vc = tietou.getVc();
        Integer axlenum = tietou.getAxlenum();
        //通过轴数获得载重限制
        Integer weightLimit = ConfigConsts.AXLENUM_WEIGHT.get(axlenum);
        //车型小于10代表车辆是客车，不标记短途重载和长途轻载
        if (vc == null || axlenum == null || vc < 10 || axlenum == 0 || weightLimit == null) {
            extraction.setLongDisLightweight(0);
            extraction.setShortDisOverweight(0);
            return;
        }

        //理论最低载重
        double theoryMin = weightMax * 0.3;
        if (weightMin < theoryMin) {
            theoryMin = weightMin;
        }
        if (totalweight > weightMax * 0.8 && tolldistance <= ConfigConsts.SHORT_DISOVER_WEIGHT_MILEAGE) {
            extraction.setShortDisOverweight(1);
        } else {
            extraction.setShortDisOverweight(0);
        }
        if (totalweight != 0 && totalweight < theoryMin * 1.2 && tolldistance >= ConfigConsts.LONG_DISLIGHT_WEIGHT_MILEAGE) {
            extraction.setLongDisLightweight(1);
        } else {
            extraction.setLongDisLightweight(0);
        }
    }

    private void speedJudgement(TietouOrigin tietou, TietouFeatureExtraction extraction) {
        Integer tolldistance = tietou.getTolldistance();
        LocalDateTime entime = tietou.getEntime();
        LocalDateTime extime = tietou.getExtime();
        Integer vc = tietou.getVc();
        Integer rkId = tietou.getRkId();
        Integer ckId = tietou.getCkId();
        if (entime == null || extime == null || entime.isBefore(ConfigConsts.LAST_TIME) || tolldistance == 0 || rkId == null || ckId == null) {
            extraction.setSpeed(0);
            extraction.setLowSpeed(0);
            extraction.setHighSpeed(0);
            return;
        }

        //查询该路段该的平均速度
        StationFeatureStatistics statistics = stationFeatureStatisticsMapper.selectByCkIdAndRkId(ckId, rkId);
        BigDecimal avgSpeedByVc;
        if (statistics == null || (avgSpeedByVc = statistics.getAvgSpeedByVc(vc)) == null) {
            log.info("未查询到该路段的平均速度，ckId: {}, rkId: {}", ckId, rkId);
            extraction.setSpeed(0);
            extraction.setLowSpeed(0);
            extraction.setHighSpeed(0);
            return;
        }

        long seconds = Duration.between(entime, extime).getSeconds();
        double speedPerHour = (double) tolldistance / seconds * 3.6;
        //小于20km/h且小于平均速度20km/h，大于180km/h且大于平均速度50km/h
        if (speedPerHour > 0 && speedPerHour <= ConfigConsts.MIN_SPEED && speedPerHour <= avgSpeedByVc.doubleValue() - 20) {
            extraction.setLowSpeed(1);
            extraction.setHighSpeed(0);
            extraction.setSpeed(1);
        } else if (speedPerHour >= ConfigConsts.MAX_SPEED && speedPerHour >= avgSpeedByVc.doubleValue() + 50) {
            extraction.setHighSpeed(1);
            extraction.setLowSpeed(0);
            extraction.setSpeed(1);
        } else {
            extraction.setSpeed(0);
            extraction.setHighSpeed(0);
            extraction.setLowSpeed(0);
        }

    }

    private void stationFlagJudgement(TietouOrigin tietou, TietouFeatureExtraction extraction) {
        String flagstationinfo = Optional.ofNullable(tietou.getFlagstationinfo()).map(String::trim).orElse(null);
        String realflagstationinfo = Optional.ofNullable(tietou.getRealflagstationinfo()).map(String::trim).orElse(null);
        BoundSetOperations<String, Object> flagDiff = redisTemplate.boundSetOps("flagDiff");
        BoundSetOperations<String, Object> flagLost = redisTemplate.boundSetOps("flagLost");
        Integer id = tietou.getId();
        if (!StringUtils.isEmpty(flagstationinfo) && StringUtils.isEmpty(realflagstationinfo)) {
            // 不在有问题的标志站里面才标记为异常
            if (flagLost.isMember(id)) {
                extraction.setFlagstationLost(0);
            } else {
                extraction.setFlagstationLost(1);
            }

        } else {
            extraction.setFlagstationLost(0);
        }

        if (!StringUtils.isEmpty(flagstationinfo) && !StringUtils.isEmpty(realflagstationinfo) && !flagstationinfo.equals(realflagstationinfo)) {
            // 不在有问题的标志站里面才标记为异常
            if (flagDiff.isMember(id)) {
                extraction.setDiffFlagstationInfo(0);
            } else {
                extraction.setDiffFlagstationInfo(1);
            }
        } else {
            extraction.setDiffFlagstationInfo(0);
        }

    }

    private void sameStationJudgement(TietouOrigin tietou, TietouFeatureExtraction extraction) {
        Integer rkId = tietou.getRkId();
        Integer ckId = tietou.getCkId();
        LocalDateTime entime = tietou.getEntime();
        LocalDateTime extime = tietou.getExtime();
        long inOutDis = Duration.between(entime, extime).getSeconds();
        if (rkId == null || ckId == null || !rkId.equals(ckId) || inOutDis < ConfigConsts.MAX_TIME_DIS) {
            extraction.setSameStation(0);
        } else {
            extraction.setSameStation(1);
        }
    }

    private void sameCarSituationJudgement(TietouOrigin tietou, TietouFeatureExtraction extraction) {
        Integer envt = tietou.getEnvt();
        Integer vt = tietou.getVt();
        if (envt == null || vt == null || envt == 0 || vt == 0 || envt.equals(vt)) {
            extraction.setSameCarSituation(1);
        } else {
            extraction.setSameCarSituation(0);
        }
    }

    private void sameCarTypeJudgement(TietouOrigin tietou, TietouFeatureExtraction extraction) {
        Integer envc = tietou.getEnvc();
        Integer vc = tietou.getVc();
        if (envc == null || vc == null || envc == 0 || vc == 0 || envc.equals(vc)) {
            extraction.setSameCarType(1);
        } else {
            extraction.setSameCarType(0);
        }
    }

    private void sameCarNumJudgement(TietouOrigin tietou, TietouFeatureExtraction extraction, BoundHashOperations<String, String, String> uselessCarOperations) {
        Integer vlpId = tietou.getVlpId();
        Integer envlpId = tietou.getEnvlpId();
        String vlp = tietou.getVlp();
        String envlp = tietou.getEnvlp();
        //判断车牌不一致，如果相似度大于0.7则认为两个车牌相同
        if (vlpId == null || envlpId == null || vlpId.equals(envlpId) || uselessCarOperations.hasKey(String.valueOf(vlpId))
                || uselessCarOperations.hasKey(String.valueOf(envlpId))
                || Levenshtein.calc(vlp.replaceAll(ConfigConsts.HUO_SUFFIX, ""), envlp.replaceAll(ConfigConsts.HUO_SUFFIX, "")) > ConfigConsts.SAME_CAR_SCORE) {
            extraction.setSameCarNumber(1);
        } else {
            extraction.setSameCarNumber(0);
        }
    }

    /**
     * 修正车牌不一致的记录
     *
     * @param monthTime
     * @param latch
     */
    @Async("taskExecutor")
    @Transactional
    public void replaceSameCarNum(int monthTime, CountDownLatch latch) {
        log.info("开始修正:{}的车牌不一致记录", monthTime);
//        tietouFeatureExtractionMapper.replaceSameCarNumByVlpId(monthTime);
//        tietouFeatureExtractionMapper.replaceSameCarNumByEnVlpId(monthTime);
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps("car_cache_useless");
        List<TietouOrigin> tietouOrigins = tietouMapper.listNonSameCarRecordByMonth(monthTime);
        for (TietouOrigin tietouOrigin : tietouOrigins) {
            Integer vlpId = tietouOrigin.getVlpId();
            Integer envlpId = tietouOrigin.getEnvlpId();
            if (hashOperations.hasKey(String.valueOf(envlpId)) || hashOperations.hasKey(String.valueOf(vlpId))) {
                //1为车牌一致
                tietouFeatureExtractionMapper.updateSameCarNumById(tietouOrigin.getId(), 1);
            }
        }
        log.info("{},车牌不一致记录修正完成", monthTime);
        latch.countDown();
    }

    /**
     * 更新statistics表内的carType
     */
    public void updateCarType() {
        long timeMillis = System.currentTimeMillis();
        TietouCleanService currentProxy = (TietouCleanService) AopContext.currentProxy();
        Integer maxId = tietouFeatureStatisticMapper.selectMaxId();
        int distance = 1000000;
        int dis = 10000;
        for (int i = 0; i <= maxId; i += distance) {
            List<TietouFeatureStatistic> tietous = tietouFeatureStatisticMapper.listVlpIdByIdPeriod(i, i + distance);
            int size = tietous.size();
            int latchSize = size % dis == 0 ? size / dis : size / dis + 1;
            CountDownLatch latch = new CountDownLatch(latchSize);
            for (int j = 0; j < size; j += dis) {
                int nextJ = j + dis;
                int boundary = nextJ < size ? nextJ : size;
                currentProxy.updateCarTypeAction(tietous.subList(j, boundary), latch);
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                log.error("{}", e);
                Thread.currentThread().interrupt();
            } finally {
                log.info("已执行完{}条记录", i + distance - 1);
            }
        }
        log.info("所有记录已更新完成，耗时{}秒", (System.currentTimeMillis() - timeMillis) / 1000);

    }

    @Async("taskExecutor")
    @Transactional
    public void updateCarTypeAction(List<TietouFeatureStatistic> subList, CountDownLatch latch) {
        for (TietouFeatureStatistic tietouFeatureStatistic : subList) {
            Integer vlpId = tietouFeatureStatistic.getVlpId();
            Integer id = tietouFeatureStatistic.getId();
            TietouCarDic tietouCarDic = carDicMapper.selectById(vlpId);
            Integer carTypeOrigin = tietouCarDic.getCarType();
            int carType = -1;
            if (carTypeOrigin >= 10) {
                carType = 0;
            } else if (carTypeOrigin > 0) {
                carType = 1;
            }
            tietouFeatureStatisticMapper.updateCarTypeById(id, carType);
        }
        latch.countDown();
        if (latch.getCount() % 100 == 0) {
            log.info("还剩:{}");
        }
    }

    /**
     * 更改通行总次数
     */
    public void updateTransitTimes() {
        long timeMillis = System.currentTimeMillis();
        TietouCleanService currentProxy = (TietouCleanService) AopContext.currentProxy();
        Integer maxId = tietouFeatureStatisticMapper.selectMaxId();
        int distance = 500000;
        int dis = 10000;
        for (int i = 0; i <= maxId; i += distance) {
            List<TietouFeatureStatistic> tietous = tietouFeatureStatisticMapper.listVlpIdByIdPeriod(i, i + distance);
            int size = tietous.size();
            int latchSize = size % dis == 0 ? size / dis : size / dis + 1;
            CountDownLatch latch = new CountDownLatch(latchSize);
            for (int j = 0; j < size; j += dis) {
                int nextJ = j + dis;
                int boundary = nextJ < size ? nextJ : size;
                currentProxy.updateTransitTimes(tietous.subList(j, boundary), latch);
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                log.error("{}", e);
                Thread.currentThread().interrupt();
            } finally {
                log.info("已执行完{}条记录", i + distance - 1);
            }
        }
        log.info("所有记录已更新完成，耗时{}秒", (System.currentTimeMillis() - timeMillis) / 1000);
    }

    @Async("taskExecutor")
    @Transactional
    public void updateTransitTimes(List<TietouFeatureStatistic> subList, CountDownLatch latch) {
        List<StatisticCount> statisticCountList = new ArrayList<>(subList.size());
        int cycle = 0;
        for (TietouFeatureStatistic tietouFeatureStatistic : subList) {
            Integer vlpId = tietouFeatureStatistic.getVlpId();
            Integer id = tietouFeatureStatistic.getId();
            Integer count = tietouMapper.getCountByVlpId(vlpId, null);
            StatisticCount statisticCount = new StatisticCount();
            statisticCount.setId(id);
            statisticCount.setCount(count);
            statisticCountList.add(statisticCount);
            cycle++;
            if (cycle % 1000 == 0 || cycle == subList.size()) {
                tietouFeatureStatisticMapper.updatetransitTimesByBatch(statisticCountList);
                log.info("已修改的数据条数：{}", cycle);
                statisticCountList.clear();
            }

        }
        latch.countDown();
        if (latch.getCount() % 100 == 0) {
            log.info("还剩:{}");
        }
    }

    /**
     * 给长途轻载和短途重载打标记
     */
    public void markOverweightAndLightweight() {
        long timeMillis = System.currentTimeMillis();
        TietouCleanService currentProxy = (TietouCleanService) AopContext.currentProxy();
        Integer maxId = tietouMapper.selectMaxId();
        int distance = 100000;
        int dis = 10000;
        for (int i = 0; i <= maxId; i += distance) {
            List<TietouOrigin> tietous = tietouMapper.listByIdPeriod(i, i + distance);
            int size = tietous.size();
            int latchSize = size % dis == 0 ? size / dis : size / dis + 1;
            CountDownLatch latch = new CountDownLatch(latchSize);
            for (int j = 0; j < size; j += dis) {
                int nextJ = j + dis;
                int boundary = nextJ < size ? nextJ : size;
                currentProxy.statisticWeightViolation(tietous.subList(j, boundary), latch);
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                log.error("{}", e);
                Thread.currentThread().interrupt();
            } finally {
                log.info("已执行完{}条记录", i + distance - 1);
            }
        }
        log.info("所有记录已更新完成，耗时{}秒", (System.currentTimeMillis() - timeMillis) / 1000);

    }

    /**
     * 统计短途重载和长度轻载
     * 总量大于最大重量80%路程小于30KM为短途重载，总量小于最小重量的120%路程大于100km为长途轻载
     *
     * @param tietouOrigins
     * @param latch
     */
    @Async("taskExecutor")
    @Transactional
    public void statisticWeightViolation(List<TietouOrigin> tietouOrigins, CountDownLatch latch) {
        for (TietouOrigin tietouOrigin : tietouOrigins) {
            Integer vc = tietouOrigin.getVc();
            Integer envc = tietouOrigin.getEnvc();
            Integer totalweight = tietouOrigin.getTotalweight();
            Integer tolldistance = tietouOrigin.getTolldistance();
            if (((vc != null && vc > 10) || (envc != null && envc > 10)) && totalweight != null && totalweight > 0) {
                Integer vlpId = tietouOrigin.getVlpId();
                TietouCarDic carDic = carDicMapper.selectById(vlpId);
                Integer weightMax = carDic.getWeightMax();
                Integer weightMin = carDic.getWeightMin();
                TietouFeatureExtraction update = new TietouFeatureExtraction();
                update.setId(tietouOrigin.getId());
                if (weightMax != null && weightMin != null && !weightMax.equals(weightMin)) {
                    //理论最低载重
                    double theoryMin = weightMax * 0.3;
                    if (weightMin < theoryMin) {
                        theoryMin = weightMin;
                    }

                    if (totalweight > weightMax * 0.8 && tolldistance <= ConfigConsts.SHORT_DISOVER_WEIGHT_MILEAGE) {
                        update.setShortDisOverweight(1);
                    }
                    if (totalweight < theoryMin * 1.2 && tolldistance >= ConfigConsts.LONG_DISLIGHT_WEIGHT_MILEAGE) {
                        update.setLongDisLightweight(1);
                    }
                }
                if (update.getShortDisOverweight() != null || update.getLongDisLightweight() != null) {
                    tietouFeatureExtractionMapper.updateByPrimaryKeySelective(update);
                }
            }
        }
        latch.countDown();

    }

    /**
     * 设置异常车牌的useFlag为false
     */
    @Transactional
    public void settingCarUseless() {
        //电动车牌正则表达式
        Pattern pattern = Pattern.compile("^.{2}[DdFf].{5}$");
        List<TietouCarDic> tietouCarDics = carDicMapper.selectUsefulAndOverLengthCar();
        for (TietouCarDic carDic : tietouCarDics) {
            String carNo = carDic.getCarNo();
            if (!pattern.matcher(carNo).matches()) {
                carDic.setUseFlag(false);
                carDicMapper.updateCarDic(carDic.getId());
                log.info("车牌:{}", carNo);
            }
        }
    }

    /**
     * 修正速异常，速度>130且>平均速度的50%才能算速度异常,此方法将以前标记为1的记录改为0
     */
    public void updateSpeedFlag() {
        long timeMillis = System.currentTimeMillis();
        TietouCleanService currentProxy = (TietouCleanService) AopContext.currentProxy();
        int dis = 10000;
        List<TietouOrigin> tietous = tietouMapper.listSpeedIllegalRecord();
        int size = tietous.size();
        int latchSize = size % dis == 0 ? size / dis : size / dis + 1;
        CountDownLatch latch = new CountDownLatch(latchSize);
        for (int j = 0; j < size; j += dis) {
            int nextJ = j + dis;
            int boundary = nextJ < size ? nextJ : size;
            currentProxy.updateSpeedFlagAction(tietous.subList(j, boundary), latch);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

        log.info("所有记录已更新完成，耗时{}秒", (System.currentTimeMillis() - timeMillis) / 1000);
    }

    @Async("taskExecutor")
    @Transactional
    public void updateSpeedFlagAction(List<TietouOrigin> tietouOrigins, CountDownLatch latch) {
        for (TietouOrigin tietouOrigin : tietouOrigins) {
            Integer rkId = tietouOrigin.getRkId();
            Integer ckId = tietouOrigin.getCkId();
            Integer vc = tietouOrigin.getVc();
            Integer tolldistance = tietouOrigin.getTolldistance();
            LocalDateTime entime = tietouOrigin.getEntime();
            LocalDateTime extime = tietouOrigin.getExtime();
            long seconds = Duration.between(entime, extime).getSeconds();
            double speed = tolldistance.doubleValue() / seconds * 3.6;
            //查询该路段该的平均速度
            StationFeatureStatistics statistics = stationFeatureStatisticsMapper.selectByCkIdAndRkId(ckId, rkId);
            if (vc == null || (speed > 0 && speed < 20) || statistics == null || statistics.getAvgSpeedByVc(vc) == null) {
                continue;
            }
            BigDecimal avgSpeedByVc = statistics.getAvgSpeedByVc(vc);
            if (speed < avgSpeedByVc.doubleValue() + 50) {
                TietouFeatureExtraction update = new TietouFeatureExtraction();
                update.setId(tietouOrigin.getId());
                update.setSpeed(0);
                tietouFeatureExtractionMapper.updateByPrimaryKeySelective(update);
            }
        }
        latch.countDown();
        log.info("还剩:{} 条记录", latch.getCount() * 10000);
    }


    public void updateAxlenumFlag() {
        long timeMillis = System.currentTimeMillis();
        TietouCleanService currentProxy = (TietouCleanService) AopContext.currentProxy();
        int dis = 10000;
        List<TietouOrigin> tietous = tietouMapper.listAxlenumIllegalRecord();
        int size = tietous.size();
        int latchSize = size % dis == 0 ? size / dis : size / dis + 1;
        CountDownLatch latch = new CountDownLatch(latchSize);
        for (int j = 0; j < size; j += dis) {
            int nextJ = j + dis;
            int boundary = nextJ < size ? nextJ : size;
            currentProxy.updateAxlenumAction(tietous.subList(j, boundary), latch);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

        log.info("所有记录已更新完成，耗时{}秒", (System.currentTimeMillis() - timeMillis) / 1000);

    }

    @Async("taskExecutor")
    @Transactional
    public void updateAxlenumAction(List<TietouOrigin> tietouOrigins, CountDownLatch latch) {
        for (TietouOrigin tietouOrigin : tietouOrigins) {
            Integer vlpId = tietouOrigin.getVlpId();
            TietouCarDic carDic = carDicMapper.selectById(vlpId);
            if (carDic == null || carDic.getAxlenum() == null) {
                continue;
            }
            Integer axlenum = carDic.getAxlenum();
            if (axlenum.equals(tietouOrigin.getAxlenum())) {
                TietouFeatureExtraction update = new TietouFeatureExtraction();
                update.setId(tietouOrigin.getId());
                update.setDifferentZhou(0);
                tietouFeatureExtractionMapper.updateByPrimaryKeySelective(update);
            }
        }
        latch.countDown();
        log.info("还剩:{} 条记录", latch.getCount() * 10000);
    }

    /**
     * 缓存免费的车辆
     */
    public void cacheFreeCar() {
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps("free_car");
        List<TietouFeatureStatistic> featureStatisticList = tietouFeatureStatisticMapper.listFreeCar();
        Map<String, String> freeCarMap = new HashMap<>(featureStatisticList.size());
        featureStatisticList.stream().forEach(f -> {
            freeCarMap.put(String.valueOf(f.getVlpId()), f.getVlp());
        });
        hashOperations.putAll(freeCarMap);
    }

    /**
     * 更新车牌号一致的蓝牌和黄牌的最大载重和最小载重
     */
    public void updateMaxAndMinWeight(Integer start, Integer end) {
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps("sameCarNo");
        Set<Object> keys = hashOperations.keys();
        //int i = carDicMapper.updateMaxAndMinWeight(keys, null);
        if (end == null) {
            end = carDicMapper.getMaxId();
        }
        int i1 = carDicMapper.updateMaxAndMinWeight(null, start, end, true);
        log.info("修改了 {} 条数据", i1);
    }


    @Async("taskExecutor")
    public void massUpdateMaxAndMinWeight(int start, int end, CountDownLatch latch) {
        Set<Integer> set = new LinkedHashSet<>((int) ((end - start) / 0.7));
        for (int i = start; i <= end; i++) {
            set.add(i);
        }
        int affectRow = carDicMapper.updateMaxAndMinWeight(set, start, end, true);
        int affectRow2 = carDicMapper.updateMaxAndMinWeight(set, start, end, false);
        latch.countDown();
        log.info("载重已更新:{} - {},更新记录数{},剩余任务数:{}", start, end, affectRow + affectRow2, latch.getCount());
    }


    /**
     * 在将每项异常的数量统计完写入static表后，在算法跑分之前需要将is_free_car进行赋值
     * 方便排除免费的内部车辆
     *
     * @return
     */
    public void updateIsFreeCar(Integer start) {
        long timeMillis = System.currentTimeMillis();
        Integer maxId = tietouMapper.selectCurrentMaxId();
        try {
            int result = tietouFeatureStatisticMapper.updateIsFreeCar(maxId, start);
            log.info("所有记录已更新完成, 总共更新{}条，耗时{}秒", result, (System.currentTimeMillis() - timeMillis) / 1000);
        } catch (Exception e) {
            log.error("更新static表is_free_car出错！", e);
        }

    }

    /**
     * 在算法跑完得分后，将score、cheating、violation、label的值copy到static表
     * 前端车辆异常详情时要用
     *
     * @return
     */
    public void copyScore2Static() {
        long timeMillis = System.currentTimeMillis();
        try {
            int result = tietouFeatureStatisticMapper.copyScore2Static();
            log.info("所有记录已更新完成, 总共更新{}条，耗时{}秒", result, (System.currentTimeMillis() - timeMillis) / 1000);
        } catch (Exception e) {
            log.error("更新static表score、cheating、violation、label出错！", e);
        }
    }

    /**
     * 导入tietou原始数据到数据库
     */
    public void importTietou2DB(String fileName, Integer count) {
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps("car_cache");
        BoundHashOperations<String, Object, Object> stationHash = redisTemplate.boundHashOps("station_dic");
        String filePath = "C:\\test\\" + fileName + ".txt";
        List<String> list = new ArrayList<>(1000000);
        TietouCleanService currentProxy = (TietouCleanService) AopContext.currentProxy();
        try {
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {
                InputStreamReader isr = new InputStreamReader(
                        new FileInputStream(file), "UTF-8");
                BufferedReader br = new BufferedReader(isr);
                // 读取文件的方法
                String lineTxt = null;
                int row = 0;
                int empty = 0;
                int towData = 0;
                while ((lineTxt = br.readLine()) != null) {
                    if (lineTxt.equals("1234567890")) {
                        break;
                    }
                    // 用于把一个字符串分割成字符串数组
                    String[] arrStrings = lineTxt.split(",");
                    if (BeanConvertUtil.replace(arrStrings[0]).equals("entime")) {
                        continue;
                    }
                    if (arrStrings.length < 5) {
                        empty++;
                    }

                    list.add(lineTxt);
                    if (arrStrings.length > 25) {
                        towData++;
                    }
                    row++;

                    if (row % 1000000 == 0 || row == count) {
                        batchImport(hashOperations, stationHash, list, currentProxy, row);
                    }

                }
                if (list.size() > 0) {
                    log.info("while 循环外层进行兜底导入。row : {}, list.size: {}", row, list.size());
                    batchImport(hashOperations, stationHash, list, currentProxy, row);
                }

                log.info("rows: {},  empty: {}, twoData: {} ", row, empty, towData);
                br.close();
            } else {
                System.out.println("文件不存在!");
            }
        } catch (Exception e) {
            System.out.println("文件读取错误!");
        }
    }

    private void batchImport(BoundHashOperations<String, Object, Object> hashOperations, BoundHashOperations<String, Object, Object> stationHash, List<String> list, TietouCleanService currentProxy, int row) {
        int size = list.size();
        int dis = 25000;
        int latchSize = size % dis == 0 ? size / dis : size / dis + 1;
        CountDownLatch latch = new CountDownLatch(latchSize);
        for (int j = 0; j < size; j += dis) {
            int nextJ = j + dis;
            int boundary = nextJ < size ? nextJ : size;
            currentProxy.importTietou2DB(list.subList(j, boundary), latch, tietouMapper, carDicMapper, hashOperations, stationHash);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } finally {
            list.clear();
            log.info("批量导入原始数据至数据库已完成{}条", row);
        }
    }

    /**
     * @param subList
     * @param latch
     * @param tietouMapper
     * @param carDicMapper
     * @param hashOperations
     */
    @Async("taskExecutor")
    public void importTietou2DB(List<String> subList, CountDownLatch latch, TietouMapper tietouMapper, TietouCarDicMapper carDicMapper,
                                BoundHashOperations<String, Object, Object> hashOperations, BoundHashOperations<String, Object, Object> stationHash) {
        List<TietouOrigin> originList = new ArrayList<>(50000);
        int count = 0;
        try {
            for (int i = 0; i < subList.size(); i++) {
                // 用于把一个字符串分割成字符串数组
                String[] arrStrings = subList.get(i).split(",");
                TietouOrigin origin = BeanConvertUtil.convertOriginal2Tietou(arrStrings);
                String vlp = origin.getVlp();
                if (!StringUtils.isEmpty(vlp)) {
                    if (hashOperations.get(vlp) != null) {
                        origin.setVlpId((Integer) hashOperations.get(vlp));
                    } else if (EntityUtil.isNormalCarNo(vlp)) {
                        TietouCarDic carDic = new TietouCarDic();
                        carDic.setCarNo(vlp);
                        carDic.setUseFlag(true);
                        carDicMapper.insertNewCar(carDic);
                        origin.setVlpId(carDicMapper.selectByCarNo(vlp).getId());
                        hashOperations.put(vlp, origin.getVlpId());
                    }

                }
                String envlp = origin.getEnvlp();
                if (!StringUtils.isEmpty(envlp)) {
                    if (hashOperations.get(envlp) != null) {
                        origin.setEnvlpId((Integer) hashOperations.get(envlp));
                    } else if (EntityUtil.isNormalCarNo(envlp)) {
                        TietouCarDic carDic = new TietouCarDic();
                        carDic.setCarNo(envlp);
                        carDic.setUseFlag(true);
                        carDicMapper.insertNewCar(carDic);
                        origin.setEnvlpId(carDicMapper.selectByCarNo(envlp).getId());
                        hashOperations.put(envlp, origin.getEnvlpId());
                    }

                }

                String ck = origin.getCk();
                if (!StringUtils.isEmpty(ck)) {
                    if (stationHash.get(ck) != null) {
                        origin.setCkId((Integer) stationHash.get(ck));
                    } else {

                    }
                }

                String rk = origin.getRk();
                if (!StringUtils.isEmpty(rk)) {
                    if (stationHash.get(rk) != null) {
                        origin.setRkId((Integer) stationHash.get(rk));
                    } else {

                    }
                }
                originList.add(origin);
                count++;
                if (count % 5000 == 0 || count == subList.size()) {
                    tietouMapper.insertBatch(originList);
                    originList.clear();
                }
            }
        } catch (Exception e) {
            log.error("批量导入原始数据至数据库出错！", e);
        } finally {
            latch.countDown();
        }

    }

    /**
     * 统计二绕站点之间互相通行的数据
     */
    public List<StationTripCountDto> statistic2ndCount() {
        List<StationTripCountDto> tripCountList = new ArrayList<>(500);
        //二绕站点之间互相的通行记录统计
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps(localVariableConfig.getRelationCacheKey());
        if (hashOperations.size() > 0) {
            List<Object> objectList = hashOperations.values();
            for (Object o : objectList) {
                StationTripCountDto dto = new StationTripCountDto();
                if (o instanceof StationTripCountDto) {
                    dto = (StationTripCountDto) o;
                } else {
                    LinkedHashMap<String, Object> linkedHashMap = (LinkedHashMap<String, Object>) o;
                    dto.setCkId((Integer) linkedHashMap.get("ckId"));
                    dto.setCkName((String) linkedHashMap.get("ckName"));
                    dto.setRkId((Integer) linkedHashMap.get("rkId"));
                    dto.setRkName((String) linkedHashMap.get("rkName"));
                    dto.setNum((Integer) linkedHashMap.get("num"));
                    dto.setTotalCount((Integer) linkedHashMap.get("totalCount"));
                }
                tripCountList.add(dto);
            }
        } else {
            List<Integer> stationIdList = stationDicMapper.getCurrentStationId(localVariableConfig.getEnterpriseCode());
            if (CollectionUtils.isEmpty(stationIdList)) {
                return null;
            }
            List<CommonTypeCountDto> countDtoList = tietouMapper.secondStatisticCkCount(stationIdList);
            Map<Integer, Integer> stationCountMap = new HashMap<>(countDtoList.size());
            for (CommonTypeCountDto commonTypeCountDto : countDtoList) {
                stationCountMap.put(commonTypeCountDto.getType(), commonTypeCountDto.getCount());
            }
            tripCountList = tietouMapper.secondStatisticTripCount(stationIdList);
            Map<String, StationTripCountDto> map = new HashMap<>(tripCountList.size());
            for (StationTripCountDto tripCount : tripCountList) {
                tripCount.setTotalCount(stationCountMap.get(tripCount.getCkId()));
                StringBuilder sb = new StringBuilder(String.valueOf(tripCount.getRkId()));
                sb.append("-").append(tripCount.getCkId());
                map.put(sb.toString(), tripCount);
            }
            hashOperations.putAll(map);
        }

        return tripCountList;
    }

    /**
     * 统计站点之间不同车型的平均速度
     *
     * @param start
     */
    public void statisticsStationAvgSpeed(Integer start) {

    }

    /**
     * 将tietou_2019内的指定高速公路的数据筛选出来到tietou
     */
    public void filterSpecifiedData2tietou() {

        long timeMillis = System.currentTimeMillis();
        TietouCleanService currentProxy = applicationContext.getBean(TietouCleanService.class);
        List<StationRiskCountDto> stationDics = stationDicMapper.list2ndStation(localVariableConfig.getEnterpriseCode());
        Map<Integer, String> stationMap = stationDics.stream().collect(Collectors.toMap(StationRiskCountDto::getCkId, StationRiskCountDto::getCkName));
        Integer maxId = tietou2019Mapper.selectMaxId();
        int distance = 1000000;
        int dis = 100000;
        for (int i = 1; i <= maxId; i += distance) {
            int end = i + distance < maxId ? i + distance : maxId;
            List<Tietou2019> tietous = tietou2019Mapper.listAllByperoid(i + 1, end);

            int size = tietous.size();
            int latchSize = size % dis == 0 ? size / dis : size / dis + 1;
            CountDownLatch latch = new CountDownLatch(latchSize);
            for (int j = 0; j < size; j += dis) {
                int nextJ = j + dis;
                int boundary = nextJ < size ? nextJ : size;
                currentProxy.convertOriginalDataByBatch(tietous.subList(j, boundary), latch, stationMap, tietouMapper);
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                log.error("{}", e);
                Thread.currentThread().interrupt();
            } finally {
                log.info("已执行完{}条记录", i + distance);
            }
        }
        log.info("所有记录已更新完成，耗时{}秒", (System.currentTimeMillis() - timeMillis) / 1000);
    }

    @Async("taskExecutor")
    public void convertOriginalDataByBatch(List<Tietou2019> subList, CountDownLatch latch, Map<Integer, String> stationMap, TietouMapper tietouMapper) {
        List<TietouOrigin> tietouList = new ArrayList<>(subList.size());
        int count = 0;
        try {
            for (Tietou2019 record : subList) {
                Integer ckId = record.getCkId();
                Integer rkId = record.getRkId();
                count++;
                if (StringUtils.isEmpty(record.getEnvlp()) || StringUtils.isEmpty(record.getVlp())) {
                    continue;
                }
                if (ckId == null) {
                    ckId = tietouStationDicService.getOrInertByName(record.getCk());

                }
                if (rkId == null) {
                    rkId = tietouStationDicService.getOrInertByName(record.getRk());
                }


                if (stationMap.containsKey(ckId) || stationMap.containsKey(rkId)) {
                    TietouOrigin tietou = new TietouOrigin();
                    EntityUtil.copyNotNullFields(record, tietou);
                    tietou.setId(null);
                    if (tietou.getEnvlpId() == null) {
                        TietouCarDic existCar = carDicMapper.selectByCarNo(tietou.getEnvlp());
                        if (existCar != null) {
                            tietou.setEnvlpId(existCar.getId());
                        }
                    }
                    if (tietou.getVlpId() == null) {
                        TietouCarDic existCar = carDicMapper.selectByCarNo(tietou.getVlp());
                        if (existCar != null) {
                            tietou.setVlpId(existCar.getId());
                        }
                    }
                    tietouList.add(tietou);
                }
                if (count % 2000 == 0 || count == subList.size()) {
                    if (!CollectionUtils.isEmpty(tietouList)) {
                        tietouMapper.insertBatch(tietouList);
                        tietouList.clear();
                    }
                }
            }
        } catch (Exception e) {
            log.error("批量处理出错", e);
        } finally {
            latch.countDown();
        }
    }

    /**
     * 打标完成后执行sql，生成tietou_feature_statistic表的数据
     */
    public void insertStatisticTableData() {
        tietouFeatureStatisticMapper.insertStatisticDataBySql();
    }

    /**
     * 打标完成后执行sql，重新生成tietou_feature_statistic表的数据之前，先删除tietou_feature_statistic的数据
     */
    public void truncateStatisticTable() {
        tietouFeatureStatisticMapper.truncateStatistic();
    }

    /**
     * 重新计算时先删除stationStatistic表的数据
     */
    public void truncateStationStatisticTable() {
        stationFeatureStatisticsMapper.truncateData();
    }

    /**
     * 重新计算时重新插入station_feature_statistic表的数据
     */
    public void insertStationFeatureStatisticData() {
        stationFeatureStatisticsMapper.rebuildTableData();
    }

    /**
     * 将tietou_feature_extraction表内的数据按月份统计到tietou_feature_statistics_month表内
     */
    public void insertStatisticsMonthData(int monthTime) {
        int affect = tietouFeatureStatisticMapper.insertStatisticsMonthData(monthTime);
        log.info("statistics_month 统计完成,月份:{},记录数:{}", monthTime, affect);
    }

    /**
     * 删减表tietou_feature_statistics_month
     */
    public void truncateStatisticMonthTable() {
        tietouFeatureStatisticMapper.truncateStatisticMonth();
    }

    /**
     * 统计tietou_feature_statistics_month表，并新增进tietou_feature_statistics表中
     */
    public void insertStatisticsByMonth() {
        int affect = tietouFeatureStatisticMapper.insertStatisticsByMonth();
        log.info("tietou_feature_statistics表共计新增:{} 条数据", affect);
    }

    public void truncateExtractionTable() {
        tietouFeatureExtractionMapper.truncate();
    }

}
