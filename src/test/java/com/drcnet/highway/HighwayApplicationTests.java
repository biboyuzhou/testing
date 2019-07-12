package com.drcnet.highway;

import com.drcnet.highway.service.dataclean.DataCleanService;
import com.drcnet.highway.service.TietouExtractionService;
import com.drcnet.highway.service.TietouScoreGyhService;
import com.drcnet.highway.service.TietoufeatureService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HighwayApplicationTests {

    @Resource
    private TietoufeatureService tietoufeatureService;
    @Resource
    private TietouExtractionService tietouExtractionService;
    @Resource
    private TietouScoreGyhService tietouScoreGyhService;
    @Resource
    private DataCleanService dataCleanService;

    @Rollback(value = false)
    @Test
    public void contextLoads() {
        int year = 201812;
        //计算标准化后的得分并保存
        tietouExtractionService.standardFeatureScore(year);
        //计算作弊类和违规类得分并保存
        tietouScoreGyhService.statisticScore2DB(year);
    }

    @Transactional
    @Rollback(value = false)
    @Test
    public void generateScores() {

    }

    @Transactional
    @Rollback(value = false)
    @Test
    public void generateMark() {
        dataCleanService.sameRouteMark(201811);
    }

}

