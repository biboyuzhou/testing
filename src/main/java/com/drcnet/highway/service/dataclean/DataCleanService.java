package com.drcnet.highway.service.dataclean;

import com.drcnet.highway.config.LocalVariableConfig;
import com.drcnet.highway.constants.CacheKeyConsts;
import com.drcnet.highway.dao.*;
import com.drcnet.highway.dto.CheatingViolationDto;
import com.drcnet.highway.entity.TietouFeatureExtraction;
import com.drcnet.highway.entity.TietouFeatureStatisticGyh;
import com.drcnet.highway.entity.TietouOrigin;
import com.drcnet.highway.entity.dic.TietouCarDic;
import com.drcnet.highway.service.TietouSameStationFrequentlyService;
import com.drcnet.highway.service.TietouService;
import com.drcnet.highway.util.domain.CarNoUtil;
import com.drcnet.highway.vo.PageVo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/8 15:01
 * @Description:
 */
@Slf4j
@Service
public class DataCleanService {

    @Resource
    private TietouService tietouService;
    @Resource
    private TietouMapper tietouMapper;
    @Resource
    private TietouFeatureStatisticGyhMapper tietouFeatureStatisticGyhMapper;
    @Resource
    private TietouFeatureStatisticMapper tietouFeatureStatisticMapper;
    @Resource
    private TietouFeatureExtractionMapper tietouFeatureExtractionMapper;
    @Resource
    private TietouSameStationFrequentlyService tietouSameStationFrequentlyService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private TietouCarDicMapper carDicMapper;
    @Resource
    private TietouCleanService tietouCleanService;

    @Resource
    private LocalVariableConfig localVariableConfig;

    public void featureClean() {
        DataCleanService thisService = (DataCleanService) AopContext.currentProxy();
        Set<Integer> ids = tietouMapper.listAxleNumDifferIds();
        Example example = new Example(TietouFeatureExtraction.class);
        int allAmount = tietouFeatureExtractionMapper.selectCountByExample(new Example(TietouFeatureExtraction.class));
        int pageSize = 100000;
        int totalPages = allAmount / pageSize + 1;
        example.selectProperties("vlpId", "id");
        for (int i = 1; i <= totalPages; i++) {
            PageHelper.startPage(i, pageSize);
            List<TietouFeatureExtraction> copyList = tietouFeatureExtractionMapper.selectByExample(example);
            PageVo<TietouFeatureExtraction> pageVo = PageVo.of(copyList);
            List<TietouFeatureExtraction> data = pageVo.getData();
            CountDownLatch latch = new CountDownLatch(data.size());
            for (TietouFeatureExtraction extraction12Copy : data) {
                thisService.update(ids, extraction12Copy, latch);
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            int reduce = allAmount - i * pageSize;
            log.info("总数:{},剩余数量:{}", allAmount, reduce > 0 ? reduce : 0);
        }

    }

    @Async("taskExecutor")
    @Transactional
    public void update(Set<Integer> ids, TietouFeatureExtraction extraction12Copy, CountDownLatch latch) {
        if (ids.contains(extraction12Copy.getVlpId())) {
            extraction12Copy.setDifferentZhou(1);
            tietouFeatureExtractionMapper.updateByPrimaryKeySelective(extraction12Copy);
        }
        latch.countDown();
    }

    /**
     * 将得分表内的数据持久化至作弊类，违规类，综合类得分表
     */
    @Transactional
    public void persistence(Integer beginMonth) {
        PageVo<CheatingViolationDto> pageVo = tietouService.listCheatingCar(0, 0, beginMonth);
        List<CheatingViolationDto> data = pageVo.getData();
        LocalDateTime now = LocalDateTime.now();
        List<TietouFeatureStatisticGyh> scores = data.stream().map(var -> {
            TietouFeatureStatisticGyh tietouFeatureStatisticGyh = new TietouFeatureStatisticGyh(var);
//            tietouFeatureStatisticGyh.setCreateTime(now);
//            tietouFeatureStatisticGyh.setMonthTime(beginMonth);
            return tietouFeatureStatisticGyh;
        }).collect(Collectors.toList());
        tietouFeatureStatisticGyhMapper.insertList(scores);
    }


    @Async("taskExecutor")
    @Transactional
    public void sameTimeRangeAction(TietouOrigin tietouOrigin, Integer monthTime, LocalDateTime firstTime, CountDownLatch latch, Set<Integer> idSet, BoundHashOperations<String, String, String> hashOperations) {
        Integer tietouId = tietouOrigin.getId();
        if (hashOperations.hasKey(String.valueOf(tietouOrigin.getVlpId()))) {
            latch.countDown();
            if (latch.getCount() % 100000 == 0) {
                log.info("已处理 {} 条记录,剩余数量:{}", latch.getCount());
            }
            return;
        }
        List<Integer> ids = tietouMapper.listSamePeriodId(tietouOrigin.getVlpId(), tietouOrigin.getEntime(), tietouOrigin.getExtime(), monthTime, firstTime);
        if (!CollectionUtils.isEmpty(ids)) {
            ids.add(tietouId);
            List<Integer> usefulIds = getUsefulIds(idSet, ids);
            if (!usefulIds.isEmpty()) {
                tietouFeatureExtractionMapper.updateSameTimeRangeByIds(usefulIds);
            }
        }
        latch.countDown();
        if (latch.getCount() % 100000 == 0) {
            log.info("已处理 {} 条记录,剩余数量:{}", latch.getCount());
        }
    }

    //如果idset包含id，则不进行更新，避免mysql死锁
    private List<Integer> getUsefulIds(Set<Integer> idSet, List<Integer> ids) {
        List<Integer> newIds = new ArrayList<>();
        for (Integer id : ids) {
            if (!idSet.contains(id)) {
                newIds.add(id);
                idSet.add(id);
            }
        }
        return newIds;
    }


    /**
     * 将同时进行的路程按车牌号和行驶时间进行标记
     */
    public void sameRouteMark(Integer beginMonth) {
        String extractionName = "tietou_feature_extraction_copy" + beginMonth;
        String originName = "first" + beginMonth;
        List<Integer> ids = tietouFeatureExtractionMapper.listSameRouteCarId(extractionName);
        DataCleanService dataCleanService = (DataCleanService) AopContext.currentProxy();
        CountDownLatch latch = new CountDownLatch(ids.size());
        for (Integer vlpId : ids) {
            dataCleanService.markSameRoute(vlpId, extractionName, originName, latch);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        log.info("标记完成！");
    }

    /**
     * 将车牌的通行时间段重叠记录查询出来，进行分组
     *
     * @param vlpId
     * @param extractionName
     * @param latch
     */
    @Transactional
    @Async("taskExecutor")
    public void markSameRoute(Integer vlpId, String extractionName, String originName, CountDownLatch latch) {
        List<TietouOrigin> tietouOrigins = tietouFeatureExtractionMapper.listAllSameRouteByCar(vlpId, extractionName, originName);
        List<Set<Integer>> allGroups = getAllGroups(tietouOrigins);
        Map<Integer, Set<Integer>> allGroupsMap = new LinkedHashMap<>();

        for (int i = 0; i < allGroups.size(); i++) {
            allGroupsMap.put(i + 1, allGroups.get(i));
        }
        for (TietouOrigin tietouOrigin : tietouOrigins) {
            StringBuilder builder = new StringBuilder();
            Integer id = tietouOrigin.getId();
            for (Map.Entry<Integer, Set<Integer>> entry : allGroupsMap.entrySet()) {
                Integer key = entry.getKey();
                Set<Integer> value = entry.getValue();
                if (value.contains(id)) {
                    if (builder.length() != 0) {
                        builder.append(",");
                    }
                    builder.append(key);
                }
            }
            tietouFeatureExtractionMapper.updateSameRouteMark(tietouOrigin.getId(), builder.toString(), extractionName);
        }
        latch.countDown();
    }

    /**
     * 获得每条记录的各种组合
     *
     * @param tietouOrigins
     * @return
     */
    private List<Set<Integer>> getAllGroups(List<TietouOrigin> tietouOrigins) {
        Map<Integer, Set<Integer>> groupMap = new HashMap<>();
        for (TietouOrigin tietouOrigin : tietouOrigins) {
            LocalDateTime entime = tietouOrigin.getEntime();
            LocalDateTime extime = tietouOrigin.getExtime();
            Integer id = tietouOrigin.getId();
            TreeSet<Integer> treeSet = new TreeSet<>();
            for (TietouOrigin origin : tietouOrigins) {
                if (tietouOrigin == origin) {
                    continue;
                }
                LocalDateTime otherEntime = origin.getEntime();
                LocalDateTime otherExtime = origin.getExtime();
                Integer otherId = origin.getId();
                if (otherEntime.isAfter(entime) && otherEntime.isBefore(extime) || otherExtime.isAfter(entime) && otherExtime.isBefore(extime)) {
                    treeSet.add(id);
                    treeSet.add(otherId);
                }
            }
            if (!treeSet.isEmpty()) {
                groupMap.put(id, treeSet);
            }
        }
        List<Set<Integer>> res = groupMap.values().stream().distinct().collect(Collectors.toList());
        Iterator<Set<Integer>> iterator = res.iterator();
        while (iterator.hasNext()) {
            Set<Integer> next = iterator.next();
            for (Set<Integer> re : res) {
                if (!re.equals(next) && re.containsAll(next)) {
                    iterator.remove();
                    break;
                }
            }
        }
        return res;
    }

    /**
     * 删除缓存数据
     *
     * @param key
     */
    public void deleteCache(String key) {
        redisTemplate.delete(key);
    }


    /**
     * 删除首页缓存数据
     */
    @CacheEvict(value = "riskProportion", allEntries = true)
    public void deleteFirstPageCache() {
        redisTemplate.delete(localVariableConfig.getRelationCacheKey());
        redisTemplate.delete(localVariableConfig.getRiskMapCacheKey());
    }

    /**
     * 重构首页缓存
     */
    public void rebuildCache() {
        tietouCleanService.statistic2ndCount();

        tietouService.statistic2ndStationRiskCount();
    }

    public void listUselessCarsWithUseFlagTrue() {
        BoundHashOperations<String, String, String> hashOperations = redisTemplate.boundHashOps(CacheKeyConsts.USELESS_CAR_USE_FLAG_TRUE);
        int amount = 0;
        Integer maxId = carDicMapper.getMaxId();
        int distance = 50000;
        for (int i = 0; i <= maxId; i += distance) {
            List<TietouCarDic> carDics = carDicMapper.selectByPeriod(i, i + distance);
            for (TietouCarDic carDic : carDics) {
                Integer id = carDic.getId();
                String carNo = carDic.getCarNo();
                if (!CarNoUtil.generateCarUseFlag(carNo.toUpperCase())) {
                    hashOperations.put(String.valueOf(id), carNo);
                    ++amount;
                }
            }
        }
        log.info("导入成功,共:{}条记录", amount);
    }

    /**
     * 将车牌的useFlag设置为false
     */
    @Transactional
    public void updateCarDicUseFlagByCache() {
        BoundHashOperations<String, String, String> hashOperations = redisTemplate.boundHashOps(CacheKeyConsts.USELESS_CAR_USE_FLAG_TRUE);
        BoundHashOperations<String, String, String> uselessOperations = redisTemplate.boundHashOps(CacheKeyConsts.CAR_CACHE_USELESS);
        uselessOperations.putAll(hashOperations.entries());
        Set<String> keys = hashOperations.keys();
        int size = 0;
        if (!CollectionUtils.isEmpty(keys)) {
            size = keys.size();
            List<Integer> ids = keys.stream().map(Integer::parseInt).collect(Collectors.toList());
            if (!ids.isEmpty()) {
                for (int i = 0; i < ids.size(); i += 10000) {
                    int next = i + 10000;
                    int bound = next < size ? next : size;
                    List<Integer> idList = ids.subList(i, bound);
                    Example example = Example.builder(TietouCarDic.class).build();
                    example.createCriteria().andIn("id", idList);
                    TietouCarDic tietouCarDic = new TietouCarDic();
                    tietouCarDic.setUseFlag(false);
                    carDicMapper.updateCar2Unuse(idList);
                }
            }
        }
        log.info("修改成功!共{}条数据", size);
        redisTemplate.delete(CacheKeyConsts.USELESS_CAR_USE_FLAG_TRUE);
    }

    /**
     * 将car_dic表中useFlag为false的车牌在statistics表内设置is_free_car为1
     */
    public void updateStatisticsFreeCar() {
        int affectRows = tietouFeatureStatisticMapper.updateStatisticsFreeCar();
        log.info("statistic表共修改{}条数据", affectRows);
    }

    /**
     * 删除tietou表重复数据
     */
    @Transactional
    public void deleteRepeatData(boolean tietouFlag) {
        List<String> idStrList = tietouMapper.selectRepeatIds(tietouFlag);
        if (idStrList.isEmpty()) {
            log.info("tietou表没有重复数据");
            return;
        }
        int doubleSize = idStrList.size() * 2;
        int initSize = doubleSize * 2 > 16 ? doubleSize * 2 : 16;
        List<Integer> ids = new ArrayList<>(initSize);
        for (String idStr : idStrList) {
            String[] split = idStr.split(",");
            List<Integer> idList = Stream.of(split).map(Integer::parseInt).collect(Collectors.toList());
            ids.addAll(idList.subList(0, idList.size() - 1));
        }
        int idSize = ids.size();
        int affectRows = 0;
        /*List<String> tables = Arrays.asList("highway2ndround.tietou", "highway_ndl.tietou", "highway_bgy.tietou", "highway_czl.tietou", "highway_cmfx.tietou",
                "highway_yl.tietou", "highway_yx.tietou", "highway_xg.tietou", "highway_zl.tietou", "highway_nwr.tietou", "highway_mn.tietou");
        for (String table : tables) {
            int i = tietouMapper.deleteRepeatByTableIds(table, ids);
            log.info("{}，共删除{}条", table, i);
        }*/

        //500条一批删除
        for (int i = 0; i < idSize; i += 500) {
            int j = i + 500;
            int bound = idSize < j ? idSize : j;
            affectRows += tietouMapper.deleteRepeatByIds(ids.subList(i, bound), tietouFlag);
        }
        log.info("铁投表删除重复数据成功,重复数据:{}条，已删除:{}条", idSize, affectRows);
    }
}
