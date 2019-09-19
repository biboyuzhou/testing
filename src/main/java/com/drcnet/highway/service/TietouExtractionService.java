package com.drcnet.highway.service;

import com.drcnet.highway.dao.TietouFeatureExtractionMapper;
import com.drcnet.highway.dao.TietouFeatureExtractionStandardScoreMapper;
import com.drcnet.highway.dto.response.SameTimeRangeStaticDto;
import com.drcnet.highway.entity.TietouFeatureExtraction;
import com.drcnet.highway.entity.TietouFeatureExtractionStandardScore;
import com.drcnet.highway.exception.MyException;
import com.drcnet.highway.util.EntityUtil;
import com.drcnet.highway.util.templates.BaseService;
import com.drcnet.highway.util.templates.MyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/10 14:20
 * @Description:
 */
@Slf4j
@Service
public class TietouExtractionService implements BaseService<TietouFeatureExtraction,Integer> {

    @Resource
    private TietouFeatureExtractionMapper thisMapper;
    @Resource
    private TietouFeatureExtractionStandardScoreMapper tietouFeatureExtractionStandardScoreMapper;
    @Resource
    private ApplicationContext applicationContext;

    @Override
    public MyMapper<TietouFeatureExtraction> getMapper() {
        return thisMapper;
    }

    private String featureTablePrev = "tietou_feature_extraction_score";
    private String featureGyhTablePrev = "tietou_feature_extraction_score_gyh";

    @Transactional
    public void standardFeatureScore(Integer beginMonth){
        String featureTableName = featureTablePrev + beginMonth;
        List<TietouFeatureExtraction> extractions = thisMapper.listAllRiskOiginalData(featureTableName);
        TietouFeatureExtractionStandardScore query = new TietouFeatureExtractionStandardScore();
        query.setMonthTime(beginMonth);
        LocalDateTime now = LocalDateTime.now();
        for (TietouFeatureExtraction extraction : extractions) {
            extraction.setId(null);
            extraction.standard();
            query.setCarNumId(extraction.getVlpId());
            TietouFeatureExtractionStandardScore res = tietouFeatureExtractionStandardScoreMapper.selectOne(query);
            if (res == null){
                TietouFeatureExtractionStandardScore standardScore = EntityUtil.copyNotNullFields(extraction,new TietouFeatureExtractionStandardScore());
                standardScore.setMonthTime(beginMonth);
                standardScore.setCreateTime(now);
                tietouFeatureExtractionStandardScoreMapper.insert(standardScore);
            }else {
                EntityUtil.copyNotNullFields(extraction,res);
                tietouFeatureExtractionStandardScoreMapper.updateByPrimaryKeySelective(res);
            }
        }

        log.info("导入标准化数据成功!");
    }


    /**
     * 将标准化后的异常车辆放到标准化表内
     * @param beginMonth
     */
//    @Transactional
    public void importGyhStandardScore(Integer beginMonth){
        String featureGyhTableName = featureGyhTablePrev + beginMonth;
        List<TietouFeatureExtraction> extractions = thisMapper.listAllRiskOiginalData(featureGyhTableName);
        TietouFeatureExtractionStandardScore query = new TietouFeatureExtractionStandardScore();
        query.setMonthTime(beginMonth);
        LocalDateTime now = LocalDateTime.now();
        TietouExtractionService currentProxy = (TietouExtractionService) AopContext.currentProxy();
        CountDownLatch latch = new CountDownLatch(extractions.size());
        for (TietouFeatureExtraction extraction : extractions) {
            extraction.setId(null);
            /*query.setCarNumId(extraction.getCarNumId());
            TietouFeatureExtractionStandardScore res = tietouFeatureExtractionStandardScoreMapper.selectOne(query);
            if (res == null){
                TietouFeatureExtractionStandardScore standardScore = EntityUtil.copyNotNullFields(extraction,new TietouFeatureExtractionStandardScore());
                standardScore.setMonthTime(beginMonth);
                standardScore.setCreateTime(now);
                tietouFeatureExtractionStandardScoreMapper.insert(standardScore);
            }else {
                EntityUtil.copyNotNullFields(extraction,res);
                tietouFeatureExtractionStandardScoreMapper.updateByPrimaryKeySelective(res);
            }*/
            currentProxy.addScores(beginMonth,extraction,now,latch);

        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new MyException();
        }
        log.info("导入标准化数据成功!");
    }

    @Async("taskExecutor")
    public void addScores(Integer beginMonth, TietouFeatureExtraction extraction, LocalDateTime now, CountDownLatch latch){
        TietouFeatureExtractionStandardScore standardScore = EntityUtil.copyNotNullFields(extraction,new TietouFeatureExtractionStandardScore());
        standardScore.setMonthTime(beginMonth);
        standardScore.setCreateTime(now);
        tietouFeatureExtractionStandardScoreMapper.insertSelective(standardScore);
        latch.countDown();
    }

    /**
     * 统计指定车辆的时间重叠次数及最大重叠次数
     * @param carId
     * @return
     */
    public SameTimeRangeStaticDto getSameTimeRangeStatic(Integer carId) {
        List<TietouFeatureExtraction> extractionList = thisMapper.listExtractionByCarId(carId);
        // 时间重叠id为key，重叠数量为value的map
        Map<String, Integer> map = new HashMap<>(1000);
        for(TietouFeatureExtraction extraction : extractionList) {
            String sameRouteMark = extraction.getSameRouteMark();
            if (!StringUtils.isEmpty(sameRouteMark)) {
                String[] sameRouteMarkArray = sameRouteMark.split(",");
                for (int i = 0; i < sameRouteMarkArray.length; i++) {
                    String sameId = sameRouteMarkArray[i];
                    if (map.containsKey(sameId)) {
                        map.put(sameId, map.get(sameId) + 1);
                    } else {
                        map.put(sameId, 1);
                    }
                }
            }
        }

        SameTimeRangeStaticDto staticDto = new SameTimeRangeStaticDto();
        staticDto.setTotalNum(map.size());
        if (!CollectionUtils.isEmpty(map)) {
            Collection<Integer> collection = map.values();
            Object[] objArray = collection.toArray();
            Arrays.sort(objArray);
            Integer maxNum = (Integer) objArray[objArray.length -1];
            staticDto.setMaxNum(maxNum);
        } else {
            staticDto.setMaxNum(0);
        }

        return staticDto;
    }

    /**
     * 拉取extraction data
     * @param currentTietouId
     * @param allMaxTietouId
     */
    public void pullExtractionDataFromAll(Integer currentTietouId, Integer allMaxTietouId) {
        long timeMillis = System.currentTimeMillis();
        Integer maxId = allMaxTietouId;
        Integer startId = 1;
        if (currentTietouId != null) {
            startId = currentTietouId;
        }
        int distance = 1000000;
        for (int i = startId; i <= maxId ; i += distance) {
            int boundary = i + distance < maxId ? i + distance - 1 : maxId;
            thisMapper.pullExtractionFromAll(i, boundary);
            log.info("分批从铁投总表拉取extraction表数据已执行完{}条记录", boundary);

        }
        log.info("extraction所有记录拉取完成，耗时{}秒", (System.currentTimeMillis() - timeMillis) / 1000);
    }
}
