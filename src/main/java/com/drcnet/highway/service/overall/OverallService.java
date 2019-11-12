package com.drcnet.highway.service.overall;

import com.drcnet.highway.constants.CacheKeyConsts;
import com.drcnet.highway.constants.TipsConsts;
import com.drcnet.highway.dao.Tietou2019Mapper;
import com.drcnet.highway.dao.TietouFeatureStatisticMapper;
import com.drcnet.highway.domain.StatisticRiskTypeCount;
import com.drcnet.highway.dto.RiskPeriodAmount;
import com.drcnet.highway.dto.response.StationRiskCountDto;
import com.drcnet.highway.dto.response.overall.EveryRoadRiskDataResponse;
import com.drcnet.highway.dto.response.overall.EveryRoadStationRiskResponse;
import com.drcnet.highway.dto.response.overall.MostRiskTypeResponse;
import com.drcnet.highway.exception.MyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @Author jack
 * @Date: 2019/9/29 15:46
 * @Desc:
 **/
@Service
@Slf4j
public class OverallService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private TietouFeatureStatisticMapper tietouFeatureStatisticMapper;
    @Resource
    private Tietou2019Mapper tietou2019Mapper;

    public List<EveryRoadRiskDataResponse> statisticEveryRoadRisk() {
        List<EveryRoadRiskDataResponse> responseList = new ArrayList<>(11);
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps(CacheKeyConsts.EVERY_ROAD_HIGH_RISK_DATA);
        EveryRoadRiskDataResponse second = new EveryRoadRiskDataResponse();
        second.setRoadCode("second");
        EveryRoadRiskDataResponse yl = new EveryRoadRiskDataResponse();
        yl.setRoadCode("yl");
        EveryRoadRiskDataResponse bgy = new EveryRoadRiskDataResponse();
        bgy.setRoadCode("bgy");
        EveryRoadRiskDataResponse ndl = new EveryRoadRiskDataResponse();
        ndl.setRoadCode("ndl");
        EveryRoadRiskDataResponse czl = new EveryRoadRiskDataResponse();
        czl.setRoadCode("czl");
        EveryRoadRiskDataResponse cmfx = new EveryRoadRiskDataResponse();
        cmfx.setRoadCode("cmfx");
        EveryRoadRiskDataResponse mn = new EveryRoadRiskDataResponse();
        mn.setRoadCode("mn");
        EveryRoadRiskDataResponse nwr = new EveryRoadRiskDataResponse();
        nwr.setRoadCode("nwr");
        EveryRoadRiskDataResponse xg = new EveryRoadRiskDataResponse();
        xg.setRoadCode("xg");
        EveryRoadRiskDataResponse yx = new EveryRoadRiskDataResponse();
        yx.setRoadCode("yx");
        EveryRoadRiskDataResponse zl = new EveryRoadRiskDataResponse();
        zl.setRoadCode("zl");
        if (hashOperations != null && hashOperations.size() > 0) {
            second.setHighRiskNum((Integer) hashOperations.get("second"));
            yl.setHighRiskNum((Integer) hashOperations.get("yl"));
            bgy.setHighRiskNum((Integer) hashOperations.get("bgy"));
            ndl.setHighRiskNum((Integer) hashOperations.get("ndl"));
            czl.setHighRiskNum((Integer) hashOperations.get("czl"));
            cmfx.setHighRiskNum((Integer) hashOperations.get("cmfx"));
            mn.setHighRiskNum((Integer) hashOperations.get("mn"));
            nwr.setHighRiskNum((Integer) hashOperations.get("nwr"));
            xg.setHighRiskNum((Integer) hashOperations.get("xg"));
            yx.setHighRiskNum((Integer) hashOperations.get("yx"));
            zl.setHighRiskNum((Integer) hashOperations.get("zl"));
        } else {
            second.setHighRiskNum(tietouFeatureStatisticMapper.getSecondRiskData());
            yl.setHighRiskNum(tietouFeatureStatisticMapper.getYlRiskData());
            bgy.setHighRiskNum(tietouFeatureStatisticMapper.getBgyRiskData());
            ndl.setHighRiskNum(tietouFeatureStatisticMapper.getNdlRiskData());
            czl.setHighRiskNum(tietouFeatureStatisticMapper.getCzlRiskData());
            cmfx.setHighRiskNum(tietouFeatureStatisticMapper.getCmfxRiskData());
            mn.setHighRiskNum(tietouFeatureStatisticMapper.getMnRiskData());
            nwr.setHighRiskNum(tietouFeatureStatisticMapper.getNwrRiskData());
            xg.setHighRiskNum(tietouFeatureStatisticMapper.getXgRiskData());
            yx.setHighRiskNum(tietouFeatureStatisticMapper.getYxRiskData());
            zl.setHighRiskNum(tietouFeatureStatisticMapper.getZlRiskData());

            hashOperations.put("second", second.getHighRiskNum());
            hashOperations.put("yl", yl.getHighRiskNum());
            hashOperations.put("bgy", bgy.getHighRiskNum());
            hashOperations.put("ndl", ndl.getHighRiskNum());
            hashOperations.put("czl", czl.getHighRiskNum());
            hashOperations.put("cmfx", cmfx.getHighRiskNum());
            hashOperations.put("mn", mn.getHighRiskNum());
            hashOperations.put("nwr", nwr.getHighRiskNum());
            hashOperations.put("xg", xg.getHighRiskNum());
            hashOperations.put("yx", yx.getHighRiskNum());
            hashOperations.put("zl", zl.getHighRiskNum());
        }
        responseList.add(second);
        responseList.add(yl);
        responseList.add(bgy);
        responseList.add(ndl);
        responseList.add(czl);
        responseList.add(cmfx);
        responseList.add(mn);
        responseList.add(nwr);
        responseList.add(xg);
        responseList.add(yx);
        responseList.add(zl);

        responseList.sort(Comparator.comparing(EveryRoadRiskDataResponse::getHighRiskNum).reversed());
        return responseList;
    }

    @Cacheable(value = "riskProportion",key = "99")
    public RiskPeriodAmount statisticTotalRiskData() {

        return tietouFeatureStatisticMapper.getRiskProportion();
    }

    public List<StationRiskCountDto> statisticHighRiskStation() {
        List<StationRiskCountDto> riskCountDtoList = new ArrayList<>(200);
        getEveryRoadStationRiskData(riskCountDtoList);
        riskCountDtoList.sort(Comparator.comparing(StationRiskCountDto::getHigh).reversed().thenComparing(StationRiskCountDto::getTotal));
        return riskCountDtoList.subList(0,5);
    }

    private void getEveryRoadStationRiskData(List<StationRiskCountDto> riskCountDtoList) {
        //二绕
        querySpecifyRoadData(riskCountDtoList, CacheKeyConsts.SECOND_FIRST_PAGE_RISK_MAP_CACHE_KEY);
        //宜泸
        querySpecifyRoadData(riskCountDtoList, CacheKeyConsts.YILU_FIRST_PAGE_RISK_MAP_CACHE_KEY);
        //巴广渝
        querySpecifyRoadData(riskCountDtoList, CacheKeyConsts.BAGUANGYU_FIRST_PAGE_RISK_MAP_CACHE_KEY);
        //南大梁
        querySpecifyRoadData(riskCountDtoList, CacheKeyConsts.NADALIANG_FIRST_PAGE_RISK_MAP_CACHE_KEY);
        //成自泸
        querySpecifyRoadData(riskCountDtoList, CacheKeyConsts.CHENGZILU_FIRST_PAGE_RISK_MAP_CACHE_KEY);
        //成绵复线
        querySpecifyRoadData(riskCountDtoList, CacheKeyConsts.CMFX_FIRST_PAGE_RISK_MAP_CACHE_KEY);
        //绵南
        querySpecifyRoadData(riskCountDtoList, CacheKeyConsts.MIANNAN_FIRST_PAGE_RISK_MAP_CACHE_KEY);
        //内威荣
        querySpecifyRoadData(riskCountDtoList, CacheKeyConsts.NEIWEIRONG_FIRST_PAGE_RISK_MAP_CACHE_KEY);
        //叙古
        querySpecifyRoadData(riskCountDtoList, CacheKeyConsts.XUGU_FIRST_PAGE_RISK_MAP_CACHE_KEY);
        //宜叙
        querySpecifyRoadData(riskCountDtoList, CacheKeyConsts.YIXU_FIRST_PAGE_RISK_MAP_CACHE_KEY);
        //自隆
        querySpecifyRoadData(riskCountDtoList, CacheKeyConsts.ZILONG_FIRST_PAGE_RISK_MAP_CACHE_KEY);
    }

    private void querySpecifyRoadData(List<StationRiskCountDto> riskCountDtoList, String cacheKey) {
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps(cacheKey);
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
        }
    }

    public EveryRoadStationRiskResponse statisticEveryRoadRiskStation() {
        EveryRoadStationRiskResponse response = new EveryRoadStationRiskResponse();
        List<StationRiskCountDto> secondList = new ArrayList<>();
        List<StationRiskCountDto> ylList = new ArrayList<>();
        List<StationRiskCountDto> bgyList = new ArrayList<>();
        List<StationRiskCountDto> ndlList = new ArrayList<>();
        List<StationRiskCountDto> czlList = new ArrayList<>();
        List<StationRiskCountDto> cmfxList = new ArrayList<>();
        List<StationRiskCountDto> mnList = new ArrayList<>();
        List<StationRiskCountDto> nwrList = new ArrayList<>();
        List<StationRiskCountDto> xgList = new ArrayList<>();
        List<StationRiskCountDto> yxList = new ArrayList<>();
        List<StationRiskCountDto> zlList = new ArrayList<>();

        //二绕
        querySpecifyRoadData(secondList, CacheKeyConsts.SECOND_FIRST_PAGE_RISK_MAP_CACHE_KEY);
        //宜泸
        querySpecifyRoadData(ylList, CacheKeyConsts.YILU_FIRST_PAGE_RISK_MAP_CACHE_KEY);
        //巴广渝
        querySpecifyRoadData(bgyList, CacheKeyConsts.BAGUANGYU_FIRST_PAGE_RISK_MAP_CACHE_KEY);
        //南大梁
        querySpecifyRoadData(ndlList, CacheKeyConsts.NADALIANG_FIRST_PAGE_RISK_MAP_CACHE_KEY);
        //成自泸
        querySpecifyRoadData(czlList, CacheKeyConsts.CHENGZILU_FIRST_PAGE_RISK_MAP_CACHE_KEY);
        //成绵复线
        querySpecifyRoadData(cmfxList, CacheKeyConsts.CMFX_FIRST_PAGE_RISK_MAP_CACHE_KEY);
        //绵南
        querySpecifyRoadData(mnList, CacheKeyConsts.MIANNAN_FIRST_PAGE_RISK_MAP_CACHE_KEY);
        //内威荣
        querySpecifyRoadData(nwrList, CacheKeyConsts.NEIWEIRONG_FIRST_PAGE_RISK_MAP_CACHE_KEY);
        //叙古
        querySpecifyRoadData(xgList, CacheKeyConsts.XUGU_FIRST_PAGE_RISK_MAP_CACHE_KEY);
        //宜叙
        querySpecifyRoadData(yxList, CacheKeyConsts.YIXU_FIRST_PAGE_RISK_MAP_CACHE_KEY);
        //自隆
        querySpecifyRoadData(zlList, CacheKeyConsts.ZILONG_FIRST_PAGE_RISK_MAP_CACHE_KEY);

        response.setSecond(secondList);
        response.setYl(ylList);
        response.setBgy(bgyList);
        response.setNdl(ndlList);
        response.setCzl(czlList);
        response.setCmfx(cmfxList);
        response.setMn(mnList);
        response.setNwr(nwrList);
        response.setXg(xgList);
        response.setYx(yxList);
        response.setZl(zlList);

        return response;
    }

    public Map<Integer, MostRiskTypeResponse> statisticTop3MostRiskType() {
        //Integer maxMonth = tietou2019Mapper.selectMaxMonth();

        Map<Integer, MostRiskTypeResponse> map = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2.compareTo(o1);
            }
        });
        BoundHashOperations<String, Object, Object> maxMonthCache = redisTemplate.boundHashOps(CacheKeyConsts.TOP_3_RISK_TYPE_MAX_MONTH);

        Integer maxMonth = maxMonthCache.get("maxMonth") == null ? 201908 : (Integer) maxMonthCache.get("maxMonth");
        MostRiskTypeResponse currentMonth = new MostRiskTypeResponse();
        BoundHashOperations<String, Object, Object> currentMonthCache = redisTemplate.boundHashOps(CacheKeyConsts.TOP_3_RISK_TYPE_CURRENT_MONTH);
        if (currentMonthCache.size() > 0) {
            getRiskDataFromCache(currentMonth, currentMonthCache);
        } else {
            getMaxMonthRiskData(maxMonth, currentMonth, currentMonthCache);
        }


        //get 倒数第二个月的数据
        BoundHashOperations<String, Object, Object> lastMonthCache = redisTemplate.boundHashOps(CacheKeyConsts.TOP_3_RISK_TYPE_LAST_MONTH);
        MostRiskTypeResponse lastMonth = new MostRiskTypeResponse();
        if (lastMonthCache.size() > 0) {
            getRiskDataFromCache(lastMonth, lastMonthCache);
        } else {
            lastMonth = getMonthRiskData(currentMonth, maxMonth - 1);
            putRiskData2Cache(lastMonth, lastMonthCache);
        }

        //get 倒数第三个月的数据
        BoundHashOperations<String, Object, Object> beforeLastMonthCache = redisTemplate.boundHashOps(CacheKeyConsts.TOP_3_RISK_TYPE_BEFORE_LAST_MONTH);
        MostRiskTypeResponse beforeLastMonth = new MostRiskTypeResponse();
        if (beforeLastMonthCache.size() > 0) {
            getRiskDataFromCache(beforeLastMonth, beforeLastMonthCache);
        } else {
            beforeLastMonth = getMonthRiskData(currentMonth, maxMonth - 2);
            putRiskData2Cache(beforeLastMonth, beforeLastMonthCache);
        }

        //get 倒数第四个月的数据
        BoundHashOperations<String, Object, Object> oldestMonthCache = redisTemplate.boundHashOps(CacheKeyConsts.TOP_3_RISK_TYPE_OLDEST_MONTH);
        MostRiskTypeResponse oldestMonth = new MostRiskTypeResponse();
        if (oldestMonthCache.size() > 0) {
            getRiskDataFromCache(oldestMonth, oldestMonthCache);
        } else {
            oldestMonth = getMonthRiskData(currentMonth, maxMonth - 3);
            putRiskData2Cache(oldestMonth, oldestMonthCache);
        }


        map.put(maxMonth, currentMonth);
        map.put(maxMonth - 1, lastMonth);
        map.put(maxMonth - 2, beforeLastMonth);
        map.put(maxMonth - 3, oldestMonth);
        return map;
    }

    private void getMaxMonthRiskData(Integer maxMonth, MostRiskTypeResponse currentMonth, BoundHashOperations<String, Object, Object> currentMonthCache) {
        StatisticRiskTypeCount riskTypeCount = tietou2019Mapper.statisticRiskTypeCountByMonth(maxMonth);
        Field[] fields = StatisticRiskTypeCount.class.getDeclaredFields();
        Map<String, Integer> riskMap = new HashMap<>(13);
        for (Field field : fields) {
            String fieldName = field.getName();
            try {
                field.setAccessible(true);
                riskMap.put(fieldName, (Integer) field.get(riskTypeCount));
            } catch (IllegalAccessException e) {
                log.error("{}", e);
                throw new MyException(TipsConsts.SERVER_ERROR);
            }
        }

        List<Map.Entry<String, Integer>> list = new ArrayList<>(riskMap.entrySet());
        Collections.sort(list, new MapValueComparator());


        for (int i = 0; i < 3; i++) {
            if (i == 0 ) {
                currentMonth.setFirstRiskName(list.get(i).getKey());
                currentMonth.setFirstRiskValue(list.get(i).getValue());
            }
            if (i == 1 ) {
                currentMonth.setSecondRiskName(list.get(i).getKey());
                currentMonth.setSecondRiskValue(list.get(i).getValue());
            }
            if (i == 2 ) {
                currentMonth.setThirdRiskName(list.get(i).getKey());
                currentMonth.setThirdRiskValue(list.get(i).getValue());
            }
        }

        putRiskData2Cache(currentMonth, currentMonthCache);
    }

    private void getRiskDataFromCache(MostRiskTypeResponse currentMonth, BoundHashOperations<String, Object, Object> currentMonthCache) {
        currentMonth.setFirstRiskName((String) currentMonthCache.get("firstRiskName"));
        currentMonth.setFirstRiskValue((Integer) currentMonthCache.get("firstRiskValue"));
        currentMonth.setSecondRiskName((String) currentMonthCache.get("secondRiskName"));
        currentMonth.setSecondRiskValue((Integer) currentMonthCache.get("secondRiskValue"));
        currentMonth.setThirdRiskName((String) currentMonthCache.get("thirdRiskName"));
        currentMonth.setThirdRiskValue((Integer) currentMonthCache.get("thirdRiskValue"));
    }

    private void putRiskData2Cache(MostRiskTypeResponse currentMonth, BoundHashOperations<String, Object, Object> currentMonthCache) {
        currentMonthCache.put("firstRiskName", currentMonth.getFirstRiskName());
        currentMonthCache.put("firstRiskValue", currentMonth.getFirstRiskValue());
        currentMonthCache.put("secondRiskName", currentMonth.getSecondRiskName());
        currentMonthCache.put("secondRiskValue", currentMonth.getSecondRiskValue());
        currentMonthCache.put("thirdRiskName", currentMonth.getThirdRiskName());
        currentMonthCache.put("thirdRiskValue", currentMonth.getThirdRiskValue());
    }

    private MostRiskTypeResponse getMonthRiskData(MostRiskTypeResponse currentMonth, int monthTime) {
        MostRiskTypeResponse response = new MostRiskTypeResponse();
        StatisticRiskTypeCount riskTypeCount = tietou2019Mapper.statisticRiskTypeCountByMonthAndRiskType(currentMonth.getFirstRiskName(),
                currentMonth.getSecondRiskName(), currentMonth.getThirdRiskName(), monthTime);
        Field[] fields = StatisticRiskTypeCount.class.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (fieldName.equals(currentMonth.getFirstRiskName())) {
                try {
                    field.setAccessible(true);
                    response.setFirstRiskName(currentMonth.getFirstRiskName());
                    response.setFirstRiskValue((Integer) field.get(riskTypeCount));
                } catch (IllegalAccessException e) {
                    log.error("{}", e);
                    throw new MyException(TipsConsts.SERVER_ERROR);
                }
            }
            if (fieldName.equals(currentMonth.getSecondRiskName())) {
                try {
                    field.setAccessible(true);
                    response.setSecondRiskName(currentMonth.getSecondRiskName());
                    response.setSecondRiskValue((Integer) field.get(riskTypeCount));
                } catch (IllegalAccessException e) {
                    log.error("{}", e);
                    throw new MyException(TipsConsts.SERVER_ERROR);
                }
            }
            if (fieldName.equals(currentMonth.getThirdRiskName())) {
                try {
                    field.setAccessible(true);
                    response.setThirdRiskName(currentMonth.getThirdRiskName());
                    response.setThirdRiskValue((Integer) field.get(riskTypeCount));
                } catch (IllegalAccessException e) {
                    log.error("{}", e);
                    throw new MyException(TipsConsts.SERVER_ERROR);
                }
            }

        }
        return response;
    }

    class MapValueComparator implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
            return (o2.getValue() - o1.getValue());
        }
    }
}
