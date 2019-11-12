package com.drcnet.highway.service;

import com.alibaba.fastjson.JSON;
import com.drcnet.highway.config.LocalVariableConfig;
import com.drcnet.highway.dao.StationDicMapper;
import com.drcnet.highway.dao.TietouCarDicMapper;
import com.drcnet.highway.dao.TietouFeatureStatisticMapper;
import com.drcnet.highway.dao.TietouMapper;
import com.drcnet.highway.dto.RiskMap;
import com.drcnet.highway.dto.SameCarEnvlpDto;
import com.drcnet.highway.dto.SameCarEnvlpIncludeDto;
import com.drcnet.highway.dto.request.RiskByRankRequest;
import com.drcnet.highway.entity.TietouFeatureStatistic;
import com.drcnet.highway.entity.dic.StationDic;
import com.drcnet.highway.entity.dic.TietouCarDic;
import com.drcnet.highway.service.dataclean.TietouCleanService;
import com.drcnet.highway.util.templates.BaseService;
import com.drcnet.highway.util.templates.MyMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * @Author: penghao
 * @CreateTime: 2019/6/13 15:12
 * @Description:
 */
@Service
@Slf4j
public class TietouStatisticsService implements BaseService<TietouFeatureStatistic, Integer> {
    @Resource
    private TietouFeatureStatisticMapper thisMapper;
    @Resource
    private TietouService tietouService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private TietouCarDicMapper tietouCarDicMapper;
    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private TietouMapper tietouMapper;
    @Resource
    private StationDicMapper stationDicMapper;
    @Resource
    private LocalVariableConfig localVariableConfig;
    @Resource
    private TietouCleanService tietouCleanService;

    @Override
    public MyMapper<TietouFeatureStatistic> getMapper() {
        return thisMapper;
    }

    /**
     * 查询车牌不一致里入站车牌重复出现两次及以上的车牌
     */
    public void listSameEnvlpMoreThan2() {
        TietouStatisticsService currentProxy = applicationContext.getBean(TietouStatisticsService.class);
        List<Integer> ids = thisMapper.listOver2SameCarNumVlpIds();
        int distance = 50;
        int size = ids.size();
        int latchSize = size % distance == 0 ? size / distance : size / distance + 1;
        CountDownLatch latch = new CountDownLatch(latchSize);
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps("sameCarNumOver2");
        BoundHashOperations<String, Object, Object> uselessOperations = redisTemplate.boundHashOps("car_cache_useless");
        Set<Object> finishIds = hashOperations.keys();

        for (int i = 0; i < size; i += distance) {
            int next = i + distance;
            int boundary = next < size ? next : size;
            List<Integer> departList = ids.subList(i, boundary);
            currentProxy.listSameEnvlpMoreThan2Action(departList, latch, hashOperations, uselessOperations, finishIds);

        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("{}", e);
            Thread.currentThread().interrupt();
        } finally {
            log.info("车牌一致数据统计结束");
        }
    }

    @Async("taskExecutor")
    public void listSameEnvlpMoreThan2Action(List<Integer> departList, CountDownLatch latch, BoundHashOperations<String, Object, Object> hashOperations
            , BoundHashOperations<String, Object, Object> uselessOperations, Set<Object> finishIds) {
        for (Integer vlpId : departList) {
            //已有该ID不做处理
            if (finishIds.contains(vlpId)) {
                continue;
            }
            List<SameCarEnvlpDto> sameCarEnvlpDtos = tietouService.listEnVlpByVlpId(vlpId);
            List<SameCarEnvlpDto> sameCars = getSameCar(sameCarEnvlpDtos, vlpId, uselessOperations);
            if (!sameCars.isEmpty()) {
                String vlp = sameCars.get(0).getVlp();
                SameCarEnvlpIncludeDto sameCarEnvlpIncludeDto = new SameCarEnvlpIncludeDto();
                sameCarEnvlpIncludeDto.setVlpId(vlpId);
                sameCarEnvlpIncludeDto.setVlp(vlp);
                sameCarEnvlpIncludeDto.setEnvlps(sameCars);
                hashOperations.put(String.valueOf(vlpId), sameCarEnvlpIncludeDto);
                log.info("车牌:{},id:{} 有超过两个以上同样进站车牌的记录", vlp, vlpId);
            }
        }
        latch.countDown();
        long count = latch.getCount();
        if (count % 100 == 0) {
            log.info("还剩:{}条记录", count * 50);
        }
    }

    private List<SameCarEnvlpDto> getSameCar(List<SameCarEnvlpDto> sameCarEnvlpDtos, Integer vlpId, BoundHashOperations<String, Object, Object> uselessOperations) {
        Map<Integer, SameCarEnvlpDto> map = new HashMap<>();
        for (SameCarEnvlpDto sameCarEnvlpDto : sameCarEnvlpDtos) {
            Integer envlpId = sameCarEnvlpDto.getEnvlpId();
            if (uselessOperations.hasKey(String.valueOf(envlpId))) {
                continue;
            }
            SameCarEnvlpDto carEnvlpDto = map.get(envlpId);
            if (carEnvlpDto == null) {
                sameCarEnvlpDto.setAmount(1);
                map.put(envlpId, sameCarEnvlpDto);
            } else {
                carEnvlpDto.setAmount(carEnvlpDto.getAmount() + 1);
            }
        }
        return map.values().stream().filter(var -> var.getAmount() > 1).collect(Collectors.toList());
    }

    /**
     * 将车牌不一致较多的车牌打印为excel
     */
    public void sameCarNum2Excel() {
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps("sameCarNumOver2");
        List<String> list = new ArrayList<>(100);
        Set<Object> secondIdSet = new HashSet<>(100);
        hashOperations.entries().forEach((k, v) -> {
            String jsonString = JSON.toJSONString(v);
            SameCarEnvlpIncludeDto envlpIncludeDto = JSON.parseObject(jsonString, SameCarEnvlpIncludeDto.class);
            List<SameCarEnvlpDto> envlps = envlpIncludeDto.getEnvlps();
            if (envlps.size() > 1 || envlps.get(0).getAmount() > 3) {
                list.add(envlpIncludeDto.getVlp());
                secondIdSet.add(k);
            }
        });
//        transfer2Excel(list,"d:/货车.xlsx");
        transfer2ndRound2Excel(secondIdSet);
    }

    /**
     * 统计出二绕的车
     *
     * @param values
     */
    private void transfer2ndRound2Excel(Set<Object> values) {
        List<StationDic> stationDics = stationDicMapper.select2ndRound(localVariableConfig.getEnterpriseCode());
        List<Integer> secondRoundIds = stationDics.stream().map(StationDic::getId).collect(Collectors.toList());
        List<String> secondRoundList = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(values.size());
        TietouStatisticsService currentProxy = applicationContext.getBean(TietouStatisticsService.class);
        for (Object value : values) {
            currentProxy.fill2nd(value,secondRoundIds,secondRoundList,latch);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        transfer2Excel(secondRoundList, "C:\\upload\\二绕车辆2.xlsx");

    }

    @Async("taskExecutor")
    @Transactional
    public void fill2nd(Object value, List<Integer> secondRoundIds,List<String> secondRoundList, CountDownLatch latch){
        Integer carNoId = Integer.parseInt((String) value);
        Integer roundCar = tietouMapper.is2ndRoundCar(carNoId, secondRoundIds);
        if (roundCar > 3) {
            TietouCarDic tietouCarDic = tietouCarDicMapper.selectById(carNoId);
            secondRoundList.add(tietouCarDic.getCarNo());
        }
        latch.countDown();
    }

    /**
     * 将车牌号输出至Excel
     *
     * @param list
     */
    private void transfer2Excel(List<String> list, String filePath) {
        XSSFWorkbook xssfSheets = new XSSFWorkbook();
        XSSFSheet sheet = xssfSheets.createSheet();
        for (int i = 0; i < list.size(); i++) {
            XSSFRow row = sheet.createRow(i);
            XSSFCell cell = row.createCell(0);
            cell.setCellValue(list.get(i));
        }
        File file = new File(filePath);

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream outputStream = new FileOutputStream(file);
            xssfSheets.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void sameCarNumDetail2Excel() {
        try (InputStream is = new FileInputStream("C:\\upload\\二绕车辆2.xlsx")){
            XSSFWorkbook xssfSheets = new XSSFWorkbook(is);
            XSSFWorkbook newBook = new XSSFWorkbook();
            XSSFSheet sheet = newBook.createSheet();
            XSSFSheet sheetAt = xssfSheets.getSheetAt(0);
            int lastRowNum = sheetAt.getLastRowNum();
            for (int i = 0; i < lastRowNum; i++) {
                XSSFCell cell = sheetAt.getRow(i).getCell(0);
                XSSFRow row = sheet.createRow(i);
                String carNo = cell.getStringCellValue();
                TietouCarDic tietouCarDic = tietouCarDicMapper.selectByCarNo(carNo);
                Integer vlpId = tietouCarDic.getId();
                RiskByRankRequest riskByRankRequest = new RiskByRankRequest();
                riskByRankRequest.setCarId(vlpId);
                List<RiskMap> riskMaps = tietouService.listRiskByRank(riskByRankRequest);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 重新生成tietou_feature_statistic表的数据
     */
    public void generateStatisticData() {
        tietouCleanService.truncateStatisticTable();
        tietouCleanService.insertStatisticTableData();
        tietouCleanService.updateIsFreeCar(1);
        log.info("------------------insert into statistic已完成！");
        thisMapper.pullStatisticScoreFromAll();
    }

    /**
     * 查询出在当前路段有异常行为的车辆
     * @param carIdList 车辆ID列表
     */
    public List<Integer> filterCurrentCarNoId(List<Integer> carIdList) {
        if (CollectionUtils.isEmpty(carIdList)){
            return new ArrayList<>();
        }
        return thisMapper.selectCurrentCarNoId(carIdList);
    }
}
