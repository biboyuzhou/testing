package com.drcnet.highway.service.dataclean;


import com.drcnet.highway.common.BeanConvertUtil;
import com.drcnet.highway.constants.CarSituationConsts;
import com.drcnet.highway.constants.TipsConsts;
import com.drcnet.highway.dao.*;
import com.drcnet.highway.domain.CarFlag;
import com.drcnet.highway.domain.SameCarNum;
import com.drcnet.highway.domain.StationFlag;
import com.drcnet.highway.dto.SuccessAmountDto;
import com.drcnet.highway.entity.*;
import com.drcnet.highway.entity.dic.StationDic;
import com.drcnet.highway.entity.dic.TietouCarDic;
import com.drcnet.highway.exception.MyException;
import com.drcnet.highway.service.TietouStationDicService;
import com.drcnet.highway.service.dataclean.flag.impl.CertainFlagImpl;
import com.drcnet.highway.service.dataclean.flag.impl.UncertainFlagImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * @Author jack
 * @Date: 2019/6/3 11:16
 * @Desc: 标志站比对service
 **/
@Service
@Slf4j
public class CompareService {
    @Resource
    private TietouMapper tietouMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private CertainFlagImpl certainFlagService;

    @Resource
    private UncertainFlagImpl uncertainFlagService;

    @Resource
    private TietouFeatureStatisticGyhMapper tietouFeatureStatisticGyhMapper;

    @Resource
    private TietouOriginal2019Mapper tietouOriginal2019Mapper;
    @Resource
    private StationDicMapper stationDicMapper;
    @Resource
    private TietouCarDicMapper tietouCarDicMapper;
    @Resource
    private TietouFeatureExtractionMapper tietouFeatureExtractionMapper;
    @Resource
    private TietouFeatureStatisticMonthMapper tietouFeatureStatisticMonthMapper;
    @Resource
    private TietouFeatureStatisticMapper tietouFeatureStatisticMapper;

    @Resource
    private TietouStationDicService tietouStationDicService;

    /**
     * 铁头给的二绕西出入站口map
     */
    private static Map<Integer, Integer> secondRoundMap = new HashMap<Integer, Integer>(13);

    static {
        secondRoundMap.put(34, 34);
        secondRoundMap.put(89, 89);
        secondRoundMap.put(91, 91);
        secondRoundMap.put(129, 129);
        secondRoundMap.put(134, 134);
        secondRoundMap.put(148, 148);
        secondRoundMap.put(150, 150);
        secondRoundMap.put(197, 197);
        secondRoundMap.put(226, 226);
        secondRoundMap.put(400, 400);
        secondRoundMap.put(401, 401);
        secondRoundMap.put(468, 468);
        secondRoundMap.put(487, 487);
    }

    /**
     * 对比理论标志站和虚拟标志站
     */
    public void cacheStationFlagInfo() {
        redisTemplate.delete("abnormal_station");
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps("abnormal_station");
        long timeMillis = System.currentTimeMillis();

        CompareService currentProxy = applicationContext.getBean(CompareService.class);
        Integer maxId = 4462616;
        Map<String, String> flagMap = new HashMap<>(10000);
        TreeMap<String, StationFlag> treeMap = new TreeMap<>();
        int distance = 1000000;
        int dis = 100000;
        for (int i = 0; i <= maxId; i += distance) {
            List<TietouOrigin> tietous = tietouMapper.listStationByPeroid(i, i + distance);
            log.info("已执行完{}条记录", i + distance);
            int size = tietous.size();
            int latchSize = size % dis == 0 ? size / dis : size / dis + 1;
            CountDownLatch latch = new CountDownLatch(latchSize);
            for (int j = 0; j < size; j += dis) {
                int nextJ = j + dis;
                int boundary = nextJ < size ? nextJ : size;
                currentProxy.cacheStationFlagInfoByBatch(tietous.subList(j, boundary), latch, hashOperations);
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
        log.info("flagMap de size wei : " + flagMap.size());
        log.info("treeMap de size wei : " + treeMap.size());
        log.info("所有记录已更新完成，耗时{}秒", (System.currentTimeMillis() - timeMillis) / 1000);
    }

    private void singleThreadProcess(List<TietouOrigin> tietouList, TreeMap<String, StationFlag> treeMap, Map<String, String> flagMap) {
        log.info("当前已缓存数据的条数: " + treeMap.size());
        for (TietouOrigin tietou : tietouList) {
            String flagstationinfo = tietou.getFlagstationinfo();
            String real = tietou.getRealflagstationinfo();
            if (StringUtils.isEmpty(flagstationinfo)) {
                continue;
            }
            int length = flagstationinfo.length();
            for (int i = 0; i < length; i += 3) {
                if ((length - i) < 3) {
                    log.info("flag 长度小于3， flag: " + flagstationinfo);
                    continue;
                }
                String flag = flagstationinfo.substring(i, i + 3);
                flagMap.put(flag, flag);
                StationFlag stationFlag;
                if (treeMap.get(flag) != null) {
                    stationFlag = treeMap.get(flag);
                } else {
                    stationFlag = new StationFlag();
                    stationFlag.setId(flag);
                }
                stationFlag.setTheoreticalFrequency(stationFlag.getTheoreticalFrequency() + 1);
                if (!StringUtils.isEmpty(real)) {
                    if (real.contains(flag)) {
                        stationFlag.setActualFrequency(stationFlag.getActualFrequency() + 1);
                    } else {
                        stationFlag.setActualAbnormal(stationFlag.getActualAbnormal() + 1);
                    }
                } else {
                    stationFlag.setActualAbnormal(stationFlag.getActualAbnormal() + 1);
                }


                treeMap.put(flag, stationFlag);
            }
        }
        log.info("本次缓存后的条数: " + treeMap.size());
    }

    /**
     * 对缓存的标志站的数据进行排序
     */
    public void sortStationFlagInfo() {
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps("second_round_car_no");
        List<Object> list = hashOperations.values();
        List<CarFlag> stationFlagList = new ArrayList<>(list.size());
        list.stream().forEach(stu -> {
            LinkedHashMap<String, Integer> linkedHashMap = (LinkedHashMap<String, Integer>) stu;
            CarFlag flag = new CarFlag();
            flag.setCarId(linkedHashMap.get("carId"));
            flag.setEnter(linkedHashMap.get("enter"));
            flag.setExit(linkedHashMap.get("exit"));
            flag.setFlag(linkedHashMap.get("flag"));
            flag.setScore(linkedHashMap.get("score"));
            flag.setSecondRound(linkedHashMap.get("secondRound"));
            stationFlagList.add(flag);
        });

        stationFlagList.sort(Comparator.comparing(CarFlag::getSecondRound).reversed().
                thenComparing(CarFlag::getScore));
        stationFlagList.forEach(System.out::println);
    }

    private void processRecord(TreeMap<String, CarFlag> treeMap, TietouOrigin tietou, Integer carNo, Integer rkId, Integer ckId, Integer score) {
        CarFlag carFlag = getCarFlag(treeMap, carNo);

        if (secondRoundMap.containsKey(rkId)) {
            carFlag.setEnter(carFlag.getEnter() + 1);
        }

        if (secondRoundMap.containsKey(ckId)) {
            carFlag.setExit(carFlag.getExit() + 1);
        }
        String flagstationinfo = StringUtils.isEmpty(tietou.getFlagstationinfo()) ? "" : tietou.getFlagstationinfo();
        /**
         * 秋燕提供二绕西的标志站
         */
        if (flagstationinfo.contains("B17") || flagstationinfo.contains("B18") || flagstationinfo.contains("B19") || flagstationinfo.contains("B1a")) {
            carFlag.setFlag(carFlag.getFlag() + 1);
        }

        if (carFlag.getSecondRound() > 0) {
            carFlag.setCarId(carNo);
            carFlag.setScore(score);
            treeMap.put(String.valueOf(carNo), carFlag);
        }
    }

    /**
     * 缓存最高得分的前4000条的车牌id
     * high_alarm_carno     货车的key
     * high_alarm_carno_ka  客车的key
     */
    public void cacheHighAlarmCarno() {
        redisTemplate.delete("high_alarm_carno_ka");
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps("high_alarm_carno_ka");
        Map<String, String> carNoMap = new HashMap<>(4000);
        List<TietouFeatureStatisticGyh> featureStatisticGyhList = tietouFeatureStatisticGyhMapper.listByPeriod();
        featureStatisticGyhList.stream().forEach(gyh -> {
            carNoMap.put(String.valueOf(gyh.getVlpId()), String.valueOf(gyh.getScore()));
        });
        hashOperations.putAll(carNoMap);
        System.out.println(hashOperations.size());
        /*Map<Object, Object> map = hashOperations.entries();
        map.entrySet();
        for(Map.Entry<Object, Object> entry : map.entrySet()) {
            System.out.println(entry.getKey() + "     " + entry.getValue());
        }*/

    }

    private CarFlag getCarFlag(TreeMap<String, CarFlag> treeMap, Integer carNo) {
        CarFlag carFlag;
        String key = String.valueOf(carNo);
        if (treeMap.get(key) != null) {
            carFlag = treeMap.get(key);
        } else {
            carFlag = new CarFlag();
            carFlag.setCarId(carNo);
        }
        return carFlag;
    }

    @Async("taskExecutor")
    public void cacheStationFlagInfoByBatch(List<TietouOrigin> tietouList, CountDownLatch latch, BoundHashOperations<String, Object, Object> hashOperations) {
        processStationFlag(tietouList, hashOperations);
        latch.countDown();
    }

    private void processStationFlag(List<TietouOrigin> tietouList, BoundHashOperations<String, Object, Object> hashOperations) {
        Map<String, String> flagMap = new HashMap<>();
        log.info("当前已缓存数据的条数: " + hashOperations.values().size());
        BigDecimal b = new BigDecimal(100);
        for (TietouOrigin tietou : tietouList) {
            String real = tietou.getRealflagstationinfo();
            String flagstationinfo = tietou.getFlagstationinfo();
            if (StringUtils.isEmpty(real)) {
                continue;
            }
            int length = real.length();
            TreeMap<String, String> treeMap = new TreeMap<>();
            for (int i = 0; i < length; i += 3) {
                if ((length - i) < 3) {
                    log.info("flag 长度小于3， flag: " + real);
                    continue;
                }
                String flag = real.substring(i, i + 3);
                if (treeMap.containsKey(flag)) {
                    if (!flagstationinfo.equals(real) && real.length() > 9 && tietou.getLastmoney().compareTo(b) > 0 ) {
                        flagMap.put(String.valueOf(tietou.getId()), String.valueOf(tietou.getId()));
                    }
                } else {
                    treeMap.put(flag, flag);
                }
            }
        }
        hashOperations.putAll(flagMap);
        log.info("本次缓存后的条数: " + hashOperations.values().size());
    }

    /**
     * 获取缓存的标志站的数量
     */
    public void getCacheStationFlagInfoSize() {
        BoundSetOperations<String, Object> boundSetOps = redisTemplate.boundSetOps("flagDiff");
        Integer id = 52375039;
        boolean b = boundSetOps.isMember(id);
        System.out.println(222);

        /*BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps("car_cache");
        Map<Object, Object> map = hashOperations.entries();
        if (map.containsKey("陕AZ5P89")) {
            System.out.println(map.get("陕AZ5P89"));
        }*/

    }

    private String getCacheKey(String carnoId, Integer zhoushu) {
        return new StringBuilder(carnoId).append("_").append(zhoushu).toString();
    }


    /**
     * 选择剔除因为标记站出问题的数据
     */
    public void chooseUnnecessaryId() {
        try {
            BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps("abnormal_station_2019060320");
            List<Object> list = hashOperations.values();
            Map<String, String> flagMap = new HashMap<>(100);
            getAssignDataFromCache(list, flagMap);

            Integer maxId = 67735963;
            int distance = 1000000;
            Set<Integer> certainFlagList = new HashSet<>(10);
            Set<Integer> uncertainFlagList = new HashSet<>(5);

            for (int i = 46124573; i <= maxId; i += distance) {
                List<TietouOrigin> tietous = tietouMapper.listStationByPeroid(i, i + distance);
                for (TietouOrigin tietou : tietous) {
                    String flagstationinfo = tietou.getFlagstationinfo();

                    if (StringUtils.isEmpty(flagstationinfo)) {
                        continue;
                    }
                    String real = tietou.getRealflagstationinfo();
                    if (StringUtils.isEmpty(real)) {

                        certainFlagService.processFlag(flagstationinfo, real, certainFlagList, flagMap, tietou.getId());
                    } else {

                        uncertainFlagService.processFlag(flagstationinfo, real, uncertainFlagList, flagMap, tietou.getId());
                    }
                }
                log.info("已执行完{}条记录", i + distance);
            }
            if (!CollectionUtils.isEmpty(certainFlagList)) {
                certainFlagService.putFlag2Cache(certainFlagList);
            }
            if (!CollectionUtils.isEmpty(uncertainFlagList)) {
                uncertainFlagService.putFlag2Cache(uncertainFlagList);
            }
            log.info("-------certainFlagList 的 size 为： {}", certainFlagList.size());
            log.info("-------uncertainFlagList 的 size 为： {}", uncertainFlagList.size());
        } catch (Exception e) {
            log.error("系统错误", e);
        }

    }

    /**
     * 从缓存的标志站数据中找出理论>0且实际标志站为0和实际标志次数占理论次数小于等于5%的数据
     *
     * @param list
     * @param flagMap
     */
    private void getAssignDataFromCache(List<Object> list, Map<String, String> flagMap) {
        list.stream().forEach(stu -> {
            LinkedHashMap<String, Object> linkedHashMap = (LinkedHashMap<String, Object>) stu;

            String flag = String.valueOf(linkedHashMap.get("id"));
            int theo = Integer.valueOf(String.valueOf(linkedHashMap.get("theoreticalFrequency"))).intValue();
            int actual = Integer.valueOf(String.valueOf(linkedHashMap.get("actualFrequency"))).intValue();

            if ((theo > 0 && actual == 0)) {
                flagMap.put(flag, flag);
            }
            if (actual > 0) {
                //BigDecimal rate = actual/theo;
                //0表示的是小数点  之前没有这样配置有问题例如  num=1 and total=1000  结果是.1  很郁闷
                DecimalFormat df = new DecimalFormat();
                //可以设置精确几位小数
                df.setMaximumFractionDigits(1);
                //模式 例如四舍五入
                df.setRoundingMode(RoundingMode.HALF_UP);

                double accuracy_num = actual * 1.0 / theo * 100;

                if (accuracy_num < 5) {
                    flagMap.put(flag, flag);
                }
            }
        });
    }

    /**
     * 将算法跑出结果最高分2000车辆与二绕取交集，并缓存结果
     * second_round_car_no_ka 客车的key
     * second_round_car_no    货车的key
     */
    public void cacheSecondHighAlarmCar() {
        redisTemplate.delete("second_round_car_no_ka");
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps("second_round_car_no_ka");
        long timeMillis = System.currentTimeMillis();
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps("high_alarm_carno_ka");

        CompareService currentProxy = applicationContext.getBean(CompareService.class);
        Integer maxId = tietouMapper.selectMaxId();
        Map<Object, Object> carNoMap = hashOps.entries();
        TreeMap<String, CarFlag> treeMap = new TreeMap<>();
        int distance = 1000000;
        for (int i = 0; i <= maxId; i += distance) {
            List<TietouOrigin> tietous = tietouMapper.listStationFlagByPeroid(i, i + distance);
            currentProxy.singleThreadProcessForSecond(tietous, treeMap, carNoMap);
            log.info("已执行完{}条记录", i + distance);
        }
        hashOperations.putAll(treeMap);
        log.info("flagMap de size wei : " + carNoMap.size());
        log.info("treeMap de size wei : " + treeMap.size());
        log.info("所有记录已更新完成，耗时{}秒", (System.currentTimeMillis() - timeMillis) / 1000);
    }

    private void singleThreadProcessForSecond(List<TietouOrigin> tietouList, TreeMap<String, CarFlag> treeMap, Map<Object, Object> carNoMap) {
        log.info("当前已缓存数据的条数: " + treeMap.size());
        for (TietouOrigin tietou : tietouList) {
            Integer envlpId = tietou.getEnvlpId();
            Integer vlpId = tietou.getVlpId();
            Integer rkId = tietou.getRkId();
            Integer ckId = tietou.getCkId();
            // 进站车牌
            Integer score;
            if (carNoMap.containsKey(String.valueOf(envlpId))) {
                score = carNoMap.get(String.valueOf(envlpId)) == null ? 0 : Integer.valueOf(String.valueOf(carNoMap.get(String.valueOf(envlpId))));
                processRecord(treeMap, tietou, envlpId, rkId, ckId, score);
            }
            // 出站出牌
            if (carNoMap.containsKey(String.valueOf(vlpId))) {
                String key = String.valueOf(vlpId);
                score = carNoMap.get(key) == null ? 0 : Integer.valueOf(String.valueOf(carNoMap.get(key)));
                processRecord(treeMap, tietou, vlpId, rkId, ckId, score);
            }


        }
        log.info("本次缓存后的条数: " + treeMap.size());
    }

    /**
     * 将original的数据转化到铁投表中
     */
    public void convertOriginal2Tietou() {
        long timeMillis = System.currentTimeMillis();
        CompareService currentProxy = applicationContext.getBean(CompareService.class);
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps("car_cache_all");
        List<StationDic> stationDics = stationDicMapper.selectAll();
        Map<String, Integer> stationMap = stationDics.stream().collect(Collectors.toMap(StationDic::getStationName, StationDic::getId));
        Integer maxId = tietouOriginal2019Mapper.selectMaxId();
        int distance = 1000000;
        int dis = 100000;
        for (int i = 0; i < maxId; i += distance) {
            int end = i + distance < maxId ? i + distance : maxId;
            List<TietouOriginal2019> tietous = tietouOriginal2019Mapper.listStationFlagByPeroid(i + 1, end);

            int size = tietous.size();
            int latchSize = size % dis == 0 ? size / dis : size / dis + 1;
            CountDownLatch latch = new CountDownLatch(latchSize);
            for (int j = 0; j < size; j += dis) {
                int nextJ = j + dis;
                int boundary = nextJ < size ? nextJ : size;
                currentProxy.convertOriginalDataByBatch(tietous.subList(j, boundary), latch, stationMap, hashOperations, tietouMapper);
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
    public void convertOriginalDataByBatch(List<TietouOriginal2019> subList, CountDownLatch latch, Map<String, Integer> stationMap, BoundHashOperations<String, Object, Object> hashOperations, TietouMapper tietouMapper) {
        List<TietouOrigin> tietouList = new ArrayList<>(subList.size());
        int count = 0;
        try {
            for (TietouOriginal2019 record : subList) {
                TietouOrigin tietou = BeanConvertUtil.convertOriginal2Tietou(record);
                Integer ckId = stationMap.get(tietou.getCk());
                Integer rkId = stationMap.get(tietou.getRk());
                Integer envlpId = (Integer) hashOperations.get(tietou.getEnvlp());
                Integer vlpId = (Integer) hashOperations.get(tietou.getVlp());
                tietou.setCkId(ckId);
                tietou.setRkId(rkId);
                tietou.setEnvlpId(envlpId);
                tietou.setVlpId(vlpId);
                tietouList.add(tietou);
                count++;
                if (count % 500 == 0 || count == subList.size()) {
                    tietouMapper.insertBatch(tietouList);
                    tietouList.clear();
                }
            }
        } catch (Exception e) {
            log.error("批量处理出错", e);
        } finally {
            latch.countDown();
        }
    }


    public void addNewCarId2Cache() {
        long timeMillis = System.currentTimeMillis();
        CompareService currentProxy = applicationContext.getBean(CompareService.class);
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps("car_cache");
        List<StationDic> stationDics = stationDicMapper.selectAll();
        Map<String, Integer> stationMap = stationDics.stream().collect(Collectors.toMap(StationDic::getStationName, StationDic::getId));
        Integer maxId = 21611570;
        int distance = 10000;
        List<TietouCarDic> carDicList = new ArrayList<>(10000);
        Map<String, String> unExistMap = new HashMap<>(10000);
        for (int i = 1; i <= 21611570; i += distance) {
            List<TietouOriginal2019> tietous = tietouOriginal2019Mapper.listStationFlagByPeroid(i, i + distance);
            currentProxy.addNewCarId2CacheByBatch(tietous, stationMap, hashOperations, carDicList, unExistMap);
            log.info("已执行完{}条记录", i + distance - 1);
        }
        tietouCarDicMapper.insertByBatch(carDicList);
        log.info("所有记录已更新完成，耗时{}秒", (System.currentTimeMillis() - timeMillis) / 1000);
    }

    private void addNewCarId2CacheByBatch(List<TietouOriginal2019> tietous, Map<String, Integer> stationMap, BoundHashOperations<String, Object, Object> hashOperations,
                                          List<TietouCarDic> carDicList, Map<String, String> unExistMap) {
        //carDic max(id)7015854
        for (TietouOriginal2019 record : tietous) {
            String ck = record.getCk();
            String rk = record.getRk();
            if (hashOperations.get(ck) == null && !unExistMap.containsKey(ck)) {
                TietouCarDic carDic = new TietouCarDic();
                carDic.setCarNo(ck);
                carDic.setUseFlag(Boolean.TRUE);
                unExistMap.put(ck, ck);
                carDicList.add(carDic);
            }

            if (hashOperations.get(rk) == null && !unExistMap.containsKey(rk)) {
                TietouCarDic carDic = new TietouCarDic();
                carDic.setCarNo(ck);
                carDic.setUseFlag(Boolean.TRUE);
                unExistMap.put(rk, rk);
                carDicList.add(carDic);
            }
        }
    }

    public static void main(String[] args) {

        String d = "川A086Xh";

        System.out.println(d.toUpperCase());

    }

    public void updateCarNoId() {
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps("car_cache");
        List<TietouOrigin> tietouOrigins = tietouMapper.listEnvlpIsNullData();
        for (TietouOrigin tietouOrigin : tietouOrigins) {
            String vlp = tietouOrigin.getVlp();
            String envlp = tietouOrigin.getEnvlp();
            if (StringUtils.isEmpty(envlp) || "null".endsWith(envlp)) {
                continue;
            }

            if (envlp.length() < 7) {
                continue;
            }
            if (hashOperations.get(vlp) == null && hashOperations.get(vlp.toUpperCase()) == null) {
                TietouCarDic carDic = tietouCarDicMapper.selectByCarNo(envlp);
                hashOperations.put(carDic.getCarNo(), carDic.getId());
                tietouOrigin.setEnvlpId(carDic.getId());

                carDic = tietouCarDicMapper.selectByCarNo(vlp);
                hashOperations.put(carDic.getCarNo(), carDic.getId());
                tietouOrigin.setVlpId(carDic.getId());
                int count = tietouMapper.updateTietou(tietouOrigin);
                if (count != 1) {
                    log.error("更新数据出错，id: {}", tietouOrigin.getId());
                }

            } else {
                Object value = hashOperations.get(envlp) == null ? hashOperations.get(envlp.toUpperCase()) : hashOperations.get(envlp);
                if (value == null) {
                    log.error("缓存数据为null，id: {}", tietouOrigin.getId());
                }
                Integer envlpId = Integer.parseInt(String.valueOf(value));
                tietouOrigin.setEnvlpId(envlpId);


                value = hashOperations.get(vlp) == null ? hashOperations.get(vlp.toUpperCase()) : hashOperations.get(vlp);
                if (value == null) {
                    log.error("缓存数据为null，id: {}", tietouOrigin.getId());
                }
                envlpId = Integer.parseInt(String.valueOf(value));
                tietouOrigin.setVlpId(envlpId);
                int count = tietouMapper.updateTietou(tietouOrigin);
                if (count != 1) {
                    log.error("更新数据出错，id: {}", tietouOrigin.getId());
                }
            }

        }
    }

    public void insertTietouByBatch() {
        long timeMillis = System.currentTimeMillis();
        Integer maxId = 69090522;
        int distance = 100000;
        int dis = 10000;
        // Integer ddd = tietouMapper.selectMaxId();
        CompareService currentProxy = applicationContext.getBean(CompareService.class);
        for (int i = 47478738; i < maxId + 1; i += distance) {
            List<TietouOrigin> tietouOriginList = tietouMapper.listStationFlagByPeroid(i, i + distance);

            int size = tietouOriginList.size();
            int latchSize = size % dis == 0 ? size / dis : size / dis + 1;
            CountDownLatch latch = new CountDownLatch(latchSize);
            for (int j = 0; j < size; j += dis) {
                int nextJ = j + dis;
                int boundary = nextJ < size ? nextJ : size;
                currentProxy.insertTietouBatch(tietouOriginList.subList(j, boundary), latch, tietouMapper);
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
    public void insertTietouBatch(List<TietouOrigin> subList, CountDownLatch latch, TietouMapper tietouMapper) {
        try {
            int step = 500;
            for (int i = 0; i < subList.size(); i += step) {
                if (i + step < subList.size()) {
                    tietouMapper.insertBatch(subList.subList(i, i + step));
                } else {
                    tietouMapper.insertBatch(subList.subList(i, subList.size()));
                }
            }

        } catch (Exception e) {
            log.error("批量处理出错", e);
        } finally {
            latch.countDown();
        }
    }

    public void updateAbnormalCarDic() {
        for (int i = 0; i < 10; i++) {
            List<TietouCarDic> ids = tietouCarDicMapper.selectAbnormalCarDic();
            if (!CollectionUtils.isEmpty(ids)) {
                for (TietouCarDic carDic : ids) {
                    int result = tietouCarDicMapper.updateCarDic(carDic.getId());
                    if (result != 1) {
                        log.error("更新carDic出错，id: {}", carDic.getId());
                    }
                }
                log.info("执行完第{}批数据.", i + 1);
            } else {
                log.info("数据处理完了。。。。。。i: {}", i);
            }

        }

    }

    public void updateZhouShuDiff() {
        BoundSetOperations<String, Object> boundSetOps = redisTemplate.boundSetOps("zhoushu_diff_vlpids");
        Set<Object> vlpIds = boundSetOps.members();
        List<Integer> vlpIdList = new ArrayList<>(vlpIds.size());
        vlpIds.stream().forEach(stu -> {
            Integer vlpId = (Integer) stu;
            vlpIdList.add(vlpId);
        });
        int dis = 1000;
        int count = 0;
        //按照vlpId进行分组
        Map<Integer, List<TietouOrigin>> groupMap = new HashMap<>(vlpIdList.size());
        getPerVlpIdMap(vlpIdList, count, groupMap);

    }

    /**
     * 得到当前车牌 每个轴数 所有的行程id数据
     *
     * @param tietouOriginList
     * @param map
     */
    private void getPerZhouShuData(List<TietouOrigin> tietouOriginList, Map<Integer, List<Integer>> map) {
        tietouOriginList.stream().forEach(tietou -> {
            Integer axlenum = tietou.getAxlenum();
            List<Integer> groupIdList;
            if (map.containsKey(axlenum)) {
                groupIdList = map.get(axlenum);
            } else {
                groupIdList = new ArrayList<>();
                map.put(axlenum, groupIdList);
            }
            groupIdList.add(tietou.getId());
        });
    }

    /**
     * 循环当前车牌每个轴数所有的行程id数据，筛选出不为默认轴数且占比大于20%的行程id，并添加到tietouIdList中
     *
     * @param tietouIdList
     * @param dicAxlenum
     * @param map
     * @param total
     */
    private void getZhouShuDiffTietouIds(List<Integer> tietouIdList, Integer dicAxlenum, Map<Integer, List<Integer>> map, int total) {
        //每个轴数 所有的tietouid
        Set<Map.Entry<Integer, List<Integer>>> entrySetAlexnum = map.entrySet();
        entrySetAlexnum.stream().forEach(data -> {
            if (data.getKey() != 0) {
                int perNum = data.getValue().size();
                // 如等于默认轴数，或者占比小于20% 则需标记为轴数正常
                if (dicAxlenum.equals(data.getKey()) || perNum * 5 < total) {
                    tietouIdList.addAll(data.getValue());
                }
            }

        });
    }

    /**
     * 得到每个vlpId对应的行程总数据
     *
     * @param vlpIdList
     * @param count
     * @param groupMap  按照车牌id对行程进行分组，得到每个车牌的行程数据
     */
    private void getPerVlpIdMap(List<Integer> vlpIdList, int count, Map<Integer, List<TietouOrigin>> groupMap) {
        log.info("总的车牌数据共有 {} 条", vlpIdList.size());
        redisTemplate.delete("different_zhou_carno");
        int max = 1000;
        int dis = 100;
        BoundSetOperations<String, Object> boundSetOps = redisTemplate.boundSetOps("different_zhou_carno");
        for (int i = 0; i < max; i += dis) {
            CompareService currentProxy = applicationContext.getBean(CompareService.class);
            int boundary = i + dis < max ? i + dis : max;
            currentProxy.filterVlpId(boundSetOps, vlpIdList.subList(i, boundary));
            log.info("查询vlp完成第{}批数据。", i);
        }

    }

    @Async("taskExecutor")
    public void filterVlpId(BoundSetOperations<String, Object> boundSetOps, List<Integer> subList) {
        List<String> carNoList = new ArrayList<>();
        for (int i = 0; i < subList.size(); i++) {
            List<TietouOrigin> tietouOriginList = tietouMapper.listByVlpId(subList.get(i));
            TietouCarDic carDic = tietouCarDicMapper.selectByPrimaryKey(subList.get(i));
            if (carDic != null && carDic.getAxlenum() != null) {
                Integer dicAxlenum = carDic.getAxlenum();

                // 当前车牌 每个轴数 所有的行程id数据
                Map<Integer, List<Integer>> map = new HashMap<>(10);

                getPerZhouShuData(tietouOriginList, map);
                int total = tietouOriginList.size();
                //每个轴数 所有的tietouid
                Set<Map.Entry<Integer, List<Integer>>> entrySetAlexnum = map.entrySet();
                entrySetAlexnum.stream().forEach(data -> {
                    if (data.getKey() != 0) {
                        int perNum = data.getValue().size();
                        // 如等于默认轴数，或者占比小于20% 则需标记为轴数正常
                        if (!dicAxlenum.equals(data.getKey()) || perNum * 10 >= total) {
                            boundSetOps.add(carDic.getCarNo());
                            carNoList.add(carDic.getCarNo());
                        }
                    }
                });
            }
        }
    }

    /**
     *
     */
    public void getZhouShuDiffInfo() {
        long timeMillis = System.currentTimeMillis();
        Integer maxId = 69090522;
        int distance = 10000000;
        /*Integer maxId = 2;
        int distance = 5;*/
        int dis = 1000000;
        CompareService currentProxy = applicationContext.getBean(CompareService.class);
        List<TietouOrigin> tietouOriginList = new ArrayList<>();
        //按照vlpId进行分组
        Map<Integer, List<TietouOrigin>> groupMap = new HashMap<>(50000);
        for (int i = 0; i < maxId + 1; i += distance) {
            int latchSize = distance % dis == 0 ? distance / dis : distance / dis + 1;
            CountDownLatch latch = new CountDownLatch(latchSize);
            int currentNum = i + distance;
            for (int j = i; j < currentNum; j += dis) {
                int boundary = j + dis < currentNum ? j + dis : currentNum;
                currentProxy.getTieTouByBatch(j, boundary, latch, tietouOriginList);
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                log.error("{}", e);
                Thread.currentThread().interrupt();
            } finally {
                log.info("当前tietouOriginList共{}条", tietouOriginList.size());
                log.info("已执行完{}条记录", i + distance);
            }
        }

        for (TietouOrigin tietouOrigin : tietouOriginList) {
            Integer vlpId = tietouOrigin.getVlpId();
            List<TietouOrigin> groupList;
            if (groupMap.containsKey(vlpId)) {
                groupList = groupMap.get(vlpId);
            } else {
                groupList = new ArrayList<>(10);
                groupMap.put(vlpId, groupList);
            }
            groupList.add(tietouOrigin);
        }

        // 将修改轴数标记的id集合
        List<Integer> tietouIdList = new ArrayList<>();
        // 每个车牌所有的行程数据
        Set<Map.Entry<Integer, List<TietouOrigin>>> entrySet = groupMap.entrySet();
        log.info("轴数不一致的车牌共有 {} 个", entrySet.size());
        entrySet.stream().forEach(entry -> {
            TietouCarDic carDic = tietouCarDicMapper.selectByPrimaryKey(entry.getKey());
            if (carDic != null && carDic.getAxlenum() != null) {
                Integer dicAxlenum = carDic.getAxlenum();
                // 当前车牌所有的行程数据
                List<TietouOrigin> tietouList = entry.getValue();
                // 当前车牌 每个轴数 所有的行程id数据
                Map<Integer, List<Integer>> map = new HashMap<>(10);

                getPerZhouShuData(tietouList, map);
                int total = tietouList.size();

                getZhouShuDiffTietouIds(tietouIdList, dicAxlenum, map, total);
            }

        });

        log.info("需更新的tietouid共有 {} 条", tietouIdList.size());

        UpdateExtractionAbnormal2Normal(tietouIdList);

        log.info("tietouOriginList 共{} 条", tietouOriginList.size());
        log.info("所有记录已更新完成，耗时{}秒", (System.currentTimeMillis() - timeMillis) / 1000);
    }

    /**
     * 修改标记表将错标为轴数异常的数据，改为轴数正常
     */
    private void UpdateExtractionAbnormal2Normal(List<Integer> tietouIdList) {
        int updateBatch = 1000;
        for (int i = 0; i < tietouIdList.size(); i += updateBatch) {
            int end = i + updateBatch;
            if (end < tietouIdList.size()) {
                int result = tietouFeatureExtractionMapper.updateExtractionZhouShuDiff(tietouIdList.subList(i, end));
                log.info("更新成功 {} 条， 当前i 为 {}", result, i);
            } else {
                int result = tietouFeatureExtractionMapper.updateExtractionZhouShuDiff(tietouIdList.subList(i, tietouIdList.size()));
                log.info("更新成功 {} 条， 当前i 为 {}", result, i);
            }
        }
    }

    /**
     * 根据起始位置查询轴数不一致的tietou表id、vkpId、alexnum
     *
     * @param start
     * @param end
     * @param latch
     * @param tietouOriginList
     */
    @Async("taskExecutor")
    public void getTieTouByBatch(int start, int end, CountDownLatch latch, List<TietouOrigin> tietouOriginList) {
        try {
            List<TietouOrigin> tietouBatch = tietouMapper.listZhouShuDIffTietouByextraction(start, end);
            tietouOriginList.addAll(tietouBatch);
        } catch (Exception e) {
            log.error("执行批量查询轴数不一致的tietou表id、vkpId、alexnum出错！", e);
        } finally {
            log.info("多线程已执行完{}条记录", end);
            latch.countDown();
        }
    }

    /**
     * 对每月的统计数据进行汇总，得到每个车牌总的异常次数，最终结果为写入tietou_feature_statistic
     */
    public void cacheStaticData() {
        redisTemplate.delete("stastic_month");
        Integer maxId = 23525737;
        int distance = 500000;
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps("stastic_month");
        //按照vlpId进行分组
        Map<String, TietouFeatureStatisticMonth> groupMap = new HashMap<>(10000000);
        for (int i = 0; i < maxId + 1; i += distance) {
            int boundary = i + distance < maxId ? i + distance : maxId;
            List<TietouFeatureStatisticMonth> monthList = tietouFeatureStatisticMonthMapper.listAllByPeriod(i, boundary);
            processData(groupMap, monthList);
            log.info("查询月表数据完成{}条", i + distance);
        }

        //hashOperations.putAll(groupMap);
        Set<Map.Entry<String, TietouFeatureStatisticMonth>> entrySet = groupMap.entrySet();
        int count = 0;
        List<TietouFeatureStatistic> statisticList = new ArrayList<>(1000);
        log.info("entrySet 的总条数: {}", entrySet.size());
        for (Map.Entry<String, TietouFeatureStatisticMonth> entry : entrySet) {
            count++;
            TietouFeatureStatistic statistic = BeanConvertUtil.convert2Statistic(entry.getValue());
            statisticList.add(statistic);
            if (count % 10000 == 0 || count == entrySet.size()) {
                tietouFeatureStatisticMapper.insertBatch(statisticList);
                log.info("已插入的数据条数：{}", count);
                statisticList.clear();
            }
        }

    }

    private void processData(Map<String, TietouFeatureStatisticMonth> groupMap, List<TietouFeatureStatisticMonth> monthList) {
        for (TietouFeatureStatisticMonth data : monthList) {
            String vlpId = String.valueOf(data.getVlpId());
            TietouFeatureStatisticMonth month;
            if (groupMap.containsKey(vlpId)) {
                month = groupMap.get(vlpId);
                month.setSameCarNumber(month.getSameCarNumber() + data.getSameCarNumber());
                month.setSpeed(month.getSpeed() + data.getSpeed());
                month.setSameCarType(month.getSameCarType() + data.getSameCarType());
                month.setSameCarSituation(month.getSameCarSituation() + data.getSameCarSituation());
                month.setShortDisOverweight(month.getShortDisOverweight() + data.getShortDisOverweight());
                month.setLongDisLightweight(month.getLongDisLightweight() + data.getLongDisLightweight());
                month.setDiffFlagstationInfo(month.getDiffFlagstationInfo() + data.getDiffFlagstationInfo());
                month.setSameStation(month.getSameStation() + data.getSameStation());
                month.setSameTimeRangeAgain(month.getSameTimeRangeAgain() + data.getSameTimeRangeAgain());
                month.setMinOutIn(month.getMinOutIn() + data.getMinOutIn());
                month.setFlagstationLost(month.getFlagstationLost() + data.getFlagstationLost());
                month.setDifferentZhou(month.getDifferentZhou() + data.getDifferentZhou());
                month.setTotal(month.getTotal() + data.getTotal());
            } else {
                month = data;
            }
            groupMap.put(vlpId, month);
        }
    }

    public void cacheTietou(int start) {
        long timeMillis = System.currentTimeMillis();
        CompareService currentProxy = applicationContext.getBean(CompareService.class);
        Integer maxId = 21611570;
        int step = 1000000;
        int distance = 100000;
        int total = step + start > maxId ? maxId - start : step;
        int end = step + start > maxId ? maxId : step + start;
        int latchSize = total % distance == 0 ? total / distance : total / distance + 1;
        if (latchSize < 10) {
            for (int i = 0; i < 10; i++) {
                String key = "tietou_cache" + i;
                redisTemplate.delete(key);
            }
        }
        CountDownLatch latch = new CountDownLatch(latchSize);
        int cacheNum = 0;
        for (int i = start; i < end; i += distance) {
            String key = "tietou_cache" + cacheNum;
            cacheNum++;
            currentProxy.getAndCacheTietou(i, distance, key, tietouMapper, latch);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("{}", e);
            Thread.currentThread().interrupt();
        } finally {
            log.info("已执行完{}条记录", end);
        }
        log.info("所有记录已更新完成，耗时{}秒", (System.currentTimeMillis() - timeMillis) / 1000);
    }

    @Async("taskExecutor")
    public void getAndCacheTietou(int i, int distance, String key, TietouMapper tietouMapper, CountDownLatch latch) {
        redisTemplate.delete(key);
        List<TietouOrigin> tietous = tietouMapper.listByPeriod(i + 1, i + distance);
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps(key);
        Map<String, TietouOrigin> map = new HashMap<>(tietous.size());
        for (TietouOrigin tietou : tietous) {
            map.put(String.valueOf(tietou.getId()), tietou);
        }
        hashOperations.putAll(map);
        log.info("已处理至：{} 条数据", i + distance);
        latch.countDown();
    }

    public void getCacheTietou(int start) {
        long timeMillis = System.currentTimeMillis();
        Integer maxId = 21611570;
        int step = 1000000;
        int total = step + start > maxId ? maxId - start : step;
        int distance = 100000;
        int latchSize = total % distance == 0 ? total / distance : total / distance + 1;
        CountDownLatch latch = new CountDownLatch(latchSize);
        CompareService currentProxy = applicationContext.getBean(CompareService.class);

        for (int i = 0; i < latchSize; i++) {
            String key = "tietou_cache" + i;
            currentProxy.batchInsertCacheTietou(key, tietouMapper, latch);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("{}", e);
            Thread.currentThread().interrupt();
        } finally {

        }
        log.info("所有记录已更新完成，耗时{}秒", (System.currentTimeMillis() - timeMillis) / 1000);

    }

    @Async("taskExecutor")
    public void batchInsertCacheTietou(String key, TietouMapper tietouMapper, CountDownLatch latch) {
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps(key);
        List<Object> objectList = new ArrayList<>();
        while (objectList.size() == 0) {
            try {
                objectList  = hashOperations.values();
            } catch (Exception e) {
                log.error("从缓存拿取数据出错! key: {}", key, e);
                objectList = hashOperations.values();
            }
        }

        try {
            List<TietouOrigin> tietouOriginList = new ArrayList<>(objectList.size());
            int count = 0;
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for(Object o : objectList) {
                TietouOrigin origin = new TietouOrigin();
                LinkedHashMap<String, Object> linkedHashMap = (LinkedHashMap<String, Object>) o;
                origin.setId((Integer) linkedHashMap.get("id"));
                origin.setMonthTime((Integer) linkedHashMap.get("monthTime"));
                origin.setEntime(LocalDateTime.parse((String)linkedHashMap.get("entime"), df));
                origin.setRk((String) linkedHashMap.get("rk"));
                origin.setRkId((Integer) linkedHashMap.get("rkId"));
                origin.setEnvlp((String) linkedHashMap.get("envlp"));
                origin.setEnvlpId((Integer) linkedHashMap.get("envlpId"));
                origin.setEnvt((Integer) linkedHashMap.get("envt"));
                origin.setEnvc((Integer) linkedHashMap.get("envc"));
                origin.setExtime(LocalDateTime.parse((String)linkedHashMap.get("extime"), df));
                origin.setCk((String) linkedHashMap.get("ck"));
                origin.setCkId((Integer) linkedHashMap.get("ckId"));
                origin.setVlp((String) linkedHashMap.get("vlp"));
                origin.setVlpId((Integer) linkedHashMap.get("vlpId"));
                origin.setVc((Integer) linkedHashMap.get("vc"));
                origin.setVt((Integer) linkedHashMap.get("vt"));
                origin.setExlane((String) linkedHashMap.get("exlane"));
                origin.setOper((String) linkedHashMap.get("oper"));
                origin.setMark((Boolean) linkedHashMap.get("mark"));
                origin.setLastmoney(BigDecimal.valueOf((Double) linkedHashMap.get("lastmoney")));
                origin.setFreemoney(BigDecimal.valueOf((Double) linkedHashMap.get("freemoney")));
                origin.setTotalweight((Integer) linkedHashMap.get("totalweight"));
                origin.setAxlenum((Integer) linkedHashMap.get("axlenum"));
                origin.setTolldistance((Integer) linkedHashMap.get("tolldistance"));
                origin.setCard((String) linkedHashMap.get("card"));
                origin.setFlagstationinfo((String) linkedHashMap.get("flagstationinfo"));
                origin.setRealflagstationinfo((String) linkedHashMap.get("realflagstationinfo"));
                origin.setInv((String) linkedHashMap.get("inv"));
                origin.setWeightLimitation((Integer) linkedHashMap.get("weightLimitation"));
                tietouOriginList.add(origin);
                count++;
                if (count % 5000 == 0 || count == objectList.size()) {
                    tietouMapper.insertBatch(tietouOriginList);
                    tietouOriginList.clear();
                    log.info("已处理完 {} 缓存的 {} 条数据", key, count);
                }
            }
            log.info("------------ {} 缓存的数据已处理完!", key, count);
            latch.countDown();
        } catch (Exception e) {
            log.error("从缓存拿到数据后，批量插入出错！key: {}", key, e);
        }

    }

    public void getSameNumCar() {
        List<SameCarNum> list = tietouMapper.getSameNumCar();
        Map<String, Integer> map = new HashMap<>(10000);
        list.stream().forEach(l -> {
            Integer vlpId = l.getVlpId();
            TietouCarDic carDic = tietouCarDicMapper.selectByPrimaryKey(vlpId);
            if (carDic.getCarType() != null) {
                List<SameCarNum> carNumList = tietouMapper.getSameNumCarByVlpId(vlpId);
                Map<Integer, Integer> vcMap = new HashMap<>(carNumList.size());
                carNumList.stream().forEach(c -> {
                    Integer vc = c.getVc();
                    if (carDic.getCarType() < 10 && vc >= 10) {
                        map.put(String.valueOf(vlpId), vc);
                    }
                    if (carDic.getCarType() >= 10 && vc < 10) {
                        map.put(String.valueOf(vlpId), vc);
                    }
                });
            }

        });
        redisTemplate.delete("sameCarNo");
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps("sameCarNo");
        hashOperations.putAll(map);
    }

    public void processSameNumCar() {
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps("sameCarNo");
        Map<Object, Object> vlpIdMap = hashOperations.entries();
        Set<Map.Entry<Object, Object>> entrySet = vlpIdMap.entrySet();

        entrySet.stream().forEach(s -> {
            Integer vlpId = Integer.parseInt(String.valueOf(s.getKey()));
            Integer vc = Integer.parseInt(String.valueOf(s.getValue()));

            TietouCarDic carDic = tietouCarDicMapper.selectById(vlpId);

            //默认车型为客车，新增车型为货车
            if (carDic.getCarType() < 10 && vc >= 10) {
                carDic.setId(null);
                carDic.setCarNo(carDic.getCarNo() + "_货");
                carDic.setCarType(vc);
                this.tietouCarDicMapper.insert(carDic);
            }

            //默认车型为货车。新增车型为客车
            if (carDic.getCarType() >= 10 && vc < 10) {
                String carNo = carDic.getCarNo();
                TietouCarDic newCar = carDic;
                carDic.setCarNo(carDic.getCarNo() + "_货");
                int result = tietouCarDicMapper.updateByPrimaryKey(carDic);
                if (result == 1) {
                    newCar.setId(null);
                    newCar.setCarType(vc);
                    newCar.setCarNo(carNo);
                    this.tietouCarDicMapper.insert(newCar);
                }
            }

        });
    }


    public void updateTietouVlpId() {
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps("sameCarNo");
        Map<Object, Object> vlpIdMap = hashOperations.entries();
        Set<Map.Entry<Object, Object>> entrySet = vlpIdMap.entrySet();
        List<TietouOrigin> updateList = new ArrayList<>(10000);
        int count = 0;
        for(Map.Entry<Object, Object> entry : entrySet) {
            Integer vlpId = Integer.parseInt(String.valueOf(entry.getKey()));
            TietouCarDic carDic = tietouCarDicMapper.selectById(vlpId);
            List<TietouOrigin> originList = tietouMapper.listByVlpId(vlpId);
            originList.stream().forEach(s -> {
                Integer vc = s.getVc();
                //默认车型为客车，新增车型为货车
                if (carDic.getCarType() < 10 && vc >= 10) {
                    TietouCarDic vlpIdHuo = tietouCarDicMapper.selectByCarNo(s.getVlp() + "_货");
                    s.setVlpId(vlpIdHuo.getId());
                    updateList.add(s);
                }

                if (carDic.getCarType() >= 10 && vc < 10) {
                    TietouCarDic vlpIdKe = tietouCarDicMapper.selectByCarNo(s.getVlp());
                    s.setVlpId(vlpIdKe.getId());
                    updateList.add(s);
                }
            });

            count++;
            if (count % 100 == 0) {
                log.info("已处理完{}条数据。", count);
            }
        }

        if (!CollectionUtils.isEmpty(updateList)) {
            updateList.stream().forEach(s -> {
                int result = tietouMapper.updateVlpIdById(s.getVlpId(), s.getId());
                if (result != 1) {
                    log.error("更新失败，id: {}, vlpI: {}", s.getId(), s.getVlpId());
                }
            });
        }
    }

    /**
     * 检查tietou表vlpId是否为car_dic_all里的id，如果不是则修改
     */
    public void checkAndUpdateTietouVlpId() {
        long timeMillis = System.currentTimeMillis();
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps("car_cache");
        Integer maxId = 21611570;
        int distance = 1000000;
        int dis = 100000;
        CompareService currentProxy = applicationContext.getBean(CompareService.class);
        for (int i = 0; i < maxId ; i += distance) {
            int boundary = i + distance < maxId ? i + distance : maxId;
            log.info("当前正执行{}条记录", boundary);
            List<TietouOrigin> tietous = tietouMapper.listByIdPeriod(i, boundary);
            int total = tietous.size();
            int latchSize = total % dis == 0 ? total / dis : total / dis + 1;
            CountDownLatch latch = new CountDownLatch(latchSize);
            for (int j = 0; j < total; j += dis) {
                int nextJ = j + dis;
                int end = nextJ < total ? nextJ : total;
                currentProxy.checkAndUpdateTietouVlpId(tietous.subList(j, end), hashOperations, tietouMapper, latch);
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                log.error("{}", e);
                Thread.currentThread().interrupt();
            } finally {
                log.info("当前tietouOriginList共{}条", tietous.size());
                log.info("已执行完{}条记录", boundary);
            }
        }
        log.info("所有记录已更新完成，耗时{}秒", (System.currentTimeMillis() - timeMillis) / 1000);
    }

    /**
     * 分批检查tietou表vlpId是否为car_dic_all里的id，如果不是则修改
     * @param subList
     * @param hashOperations
     * @param latch
     */
    @Async("taskExecutor")
    public void checkAndUpdateTietouVlpId(List<TietouOrigin> subList, BoundHashOperations<String, Object, Object> hashOperations, TietouMapper tietouMapper, CountDownLatch latch) {
        try {
            for (TietouOrigin tietouOrigin : subList) {
                String envlp = tietouOrigin.getEnvlp();
                String vlp = tietouOrigin.getVlp();
                Integer cEnvlpId = (Integer) hashOperations.get(envlp);
                Integer cVlpId = (Integer) hashOperations.get(vlp);
                Integer envlpId = null;
                Integer vlpId = null;
                //入口车牌不一致
                if (cEnvlpId != null && !cEnvlpId.equals(tietouOrigin.getEnvlpId())) {
                    envlpId = cEnvlpId;
                }
                if (cVlpId != null && !cVlpId.equals(tietouOrigin.getVlpId())) {
                    vlpId = cVlpId;
                }

                if (envlpId != null || vlpId != null) {
                    int result = tietouMapper.updateVlpIdAndEnvlpIdById(envlpId, vlpId, tietouOrigin.getId());
                }
            }
        } catch (Exception e) {
            log.error("分批检查tietou表vlpId是否为car_dic_all里的id出错。", e);
        } finally {
            latch.countDown();
        }
    }

    public SuccessAmountDto uploadNewStation(MultipartFile file) {
        SuccessAmountDto successAmountDto = new SuccessAmountDto();
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new MyException("文件异常请重试");
        }
        if (!originalFilename.endsWith("xls") && !originalFilename.endsWith("xlsx")) {
            throw new MyException("文件类型错误，请上传Excel文件");
        }
        int successAmount = 0;
        int total = 0;
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps("station_dic");
        BoundHashOperations<String, Object, Object> carCache = redisTemplate.boundHashOps("car_cache");
        List<TietouOrigin> originList = new ArrayList<>(5000);
        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            for (int i = 1; i <= lastRowNum; i++) {
                if (i < 4) {
                    continue;
                }
                TietouOrigin origin = new TietouOrigin();
                Row row = sheet.getRow(i);
                //card
                origin.setCard(row.getCell(1).getStringCellValue());

                //ck,ck_id
                Cell vlpCell = row.getCell(3);
                if (vlpCell != null && !StringUtils.isEmpty(vlpCell.getStringCellValue())) {
                    String vlp = vlpCell.getStringCellValue();
                    origin.setVlp(vlp);
                    if (carCache.get(vlp) != null) {
                        origin.setVlpId(Integer.parseInt(String.valueOf(carCache.get(vlp))));
                    }
                }

                //出口车道
                origin.setExlane(row.getCell(4).getStringCellValue());

                //
                origin.setOper(row.getCell(5).getStringCellValue());

                String extime = row.getCell(7).getStringCellValue();
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                origin.setExtime(LocalDateTime.parse(extime, df));

                origin.setVt(CarSituationConsts.SITUATION_MAP.get(row.getCell(8).getStringCellValue()));
                origin.setVc(CarSituationConsts.CAR_TYPE_MAP.get(row.getCell(9).getStringCellValue()));


                Cell rkCell = row.getCell(10);
                if (rkCell != null && !StringUtils.isEmpty(rkCell.getStringCellValue())) {
                    String rk = rkCell.getStringCellValue();
                    origin.setRk(rk);
                    if (hashOperations.get(rk) != null) {
                        origin.setRkId(Integer.parseInt(String.valueOf(hashOperations.get(rk))));
                    }
                }

                String entime = row.getCell(12).getStringCellValue();
                origin.setEntime(LocalDateTime.parse(entime, df));

                Cell envlpCell = row.getCell(13);
                if (envlpCell != null && !StringUtils.isEmpty(envlpCell.getStringCellValue())) {
                    String envlp = envlpCell.getStringCellValue();
                    origin.setEnvlp(envlp);
                    if (carCache.get(envlp) != null) {
                        origin.setEnvlpId(Integer.parseInt(String.valueOf(carCache.get(envlp))));
                    }
                }

                origin.setEnvc(Integer.parseInt(row.getCell(14).getStringCellValue()));
                origin.setEnvt(Integer.parseInt(row.getCell(15).getStringCellValue()));

                Cell ckCell = row.getCell(18);
                if (ckCell != null && !StringUtils.isEmpty(ckCell.getStringCellValue())) {
                    String ck = ckCell.getStringCellValue();
                    origin.setCk(ck);
                    if (hashOperations.get(ck) != null) {
                        origin.setCkId(Integer.parseInt(String.valueOf(hashOperations.get(ck))));
                    }
                }

                origin.setInv(row.getCell(25).getStringCellValue());

                origin.setTolldistance(Integer.parseInt(row.getCell(27).getStringCellValue()));
                origin.setTotalweight(Integer.parseInt(row.getCell(29).getStringCellValue()));
                origin.setWeightLimitation(Integer.parseInt(row.getCell(30).getStringCellValue()));
                origin.setAxlenum(Integer.parseInt(row.getCell(32).getStringCellValue()));
                origin.setLastmoney(new BigDecimal(Double.valueOf(row.getCell(34).getStringCellValue())));






            }
            successAmountDto.setSuccess(successAmount);
            successAmountDto.setTotal(total);
        } catch (IOException e) {
            log.error("{}", e);
            throw new MyException(TipsConsts.SERVER_ERROR);
        } catch (InvalidFormatException e) {
            log.error("{}", e);
            throw new MyException(TipsConsts.SERVER_ERROR);
        }
        return successAmountDto;
    }

    private void compareAndInsertStation(BoundHashOperations<String, Object, Object> hashOperations, String rk, List<StationDic> dicList) {
        if (hashOperations.get(rk) == null) {
            StationDic query = new StationDic();
            query.setStationName(rk);
            List<StationDic> stationDicList = stationDicMapper.select(query);
            if (CollectionUtils.isEmpty(stationDicList)) {
                StationDic insert = new StationDic();
                insert.setStationName(rk);
                dicList.add(insert);
            }
        }
    }

    public Integer testMycat(Integer routingId) {
        /*Integer result = tietouMapper.testMycat(routingId);
        return result;*/

        List<Long> originList = tietouMapper.testListMycat(routingId);
        return originList.size();
    }
}
