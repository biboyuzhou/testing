package com.drcnet.highway.service;

import com.drcnet.highway.dao.TietouFeatureExtractionStandardScoreMapper;
import com.drcnet.highway.dao.TietouFeatureStatisticGyhMapper;
import com.drcnet.highway.dto.RiskPeriodAmount;
import com.drcnet.highway.dto.request.CheatingListDto;
import com.drcnet.highway.entity.TietouFeatureStatisticGyh;
import com.drcnet.highway.exception.MyException;
import com.drcnet.highway.util.ExcelUtil;
import com.drcnet.highway.util.templates.BaseService;
import com.drcnet.highway.util.templates.MyMapper;
import com.drcnet.highway.vo.PageVo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/13 16:29
 * @Description:
 */
@Slf4j
@Service
public class TietouScoreGyhService implements BaseService<TietouFeatureStatisticGyh,Integer> {

    @Resource
    private TietouFeatureStatisticGyhMapper thisMapper;
    @Resource
    private TietouFeatureExtractionStandardScoreMapper standardScoreMapper;
    @Resource
    private TietouFeatureStatisticGyhMapper tietouFeatureStatisticGyhMapper;

    @Override
    public MyMapper<TietouFeatureStatisticGyh> getMapper() {
        return thisMapper;
    }

    /**
     * 查询作弊车辆列表
     * @return
     */
    public PageVo<TietouFeatureStatisticGyh> listCheatingCar(CheatingListDto dto) {
        PageHelper.startPage(dto.getPageNum(),dto.getPageSize());
        List<TietouFeatureStatisticGyh> select = new ArrayList<>();
        if (!StringUtils.isEmpty(dto.getFlags())) {
            String[] strArray = dto.getFlags().split(",");
            dto.setFields(Arrays.asList(strArray));
        }
        try {
            select = thisMapper.listCheatingCar(dto);
        }catch (Exception e) {
            log.error("查询首页统计信息出错！", e);
            throw new MyException("查询风险信息出错！");
        } finally {
            return PageVo.of(select);
        }

    }

    /**
     * 查询每种风险的数量
     * @param beginMonth
     * @param carType
     */
    @Cacheable(value = "riskProportion",key = "#beginMonth.toString().concat('-').concat(#carType)")
    public RiskPeriodAmount getRiskProportion(Integer beginMonth, Integer carType) {
        return thisMapper.getRiskProportion(carType);
    }

    /**
     * 获得excel流
     */
    public byte[] exportCheatingCar(CheatingListDto dto, String fileName) {
        dto.setPageNum(0);
        dto.setPageSize(0);
        PageVo<TietouFeatureStatisticGyh> pageVo = listCheatingCar(dto);
        List<TietouFeatureStatisticGyh> data = pageVo.getData();
        return ExcelUtil.getExportBytes(data,fileName);
    }

    public BigDecimal getCarRiskLevel(Integer carId, Integer beginMonth) {
        TietouFeatureStatisticGyh tietouFeatureStatisticGyh = new TietouFeatureStatisticGyh();
        tietouFeatureStatisticGyh.setVlpId(carId);
        tietouFeatureStatisticGyh.setMonthTime(beginMonth);
        TietouFeatureStatisticGyh res = thisMapper.selectOne(tietouFeatureStatisticGyh);
        if (res == null){
            throw new MyException("暂无此数据");
        }
        return res.getScore();
    }

    /**
     * 将tietou_feature_extaction_statistic统计数据里带有风险值的数据保存至tietou_score表内
     */
//    @Transactional
    public void statisticScore2DB(Integer beginTime){
        TietouScoreGyhService currentProxy = (TietouScoreGyhService) AopContext.currentProxy();
        //查询所有有风险的数据
        List<TietouFeatureStatisticGyh> tietouFeatureStatisticGyhs = standardScoreMapper.listCheatingAndViolationData(beginTime);

        CountDownLatch latch = new CountDownLatch(tietouFeatureStatisticGyhs.size());
        for (TietouFeatureStatisticGyh tietouFeatureStatisticGyh : tietouFeatureStatisticGyhs) {
            currentProxy.saveScore(tietouFeatureStatisticGyh,beginTime,latch);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new MyException();
        }
        log.info("同步成功");
    }

    @Async("taskExecutor")
    public void saveScore(TietouFeatureStatisticGyh tietouFeatureStatisticGyh, Integer beginTime, CountDownLatch latch){
        tietouFeatureStatisticGyh.setMonthTime(beginTime);
        tietouFeatureStatisticGyh.setCreateTime(LocalDateTime.now());
        tietouFeatureStatisticGyhMapper.insert(tietouFeatureStatisticGyh);
        latch.countDown();
    }


}
