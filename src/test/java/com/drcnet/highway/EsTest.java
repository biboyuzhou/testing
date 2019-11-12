package com.drcnet.highway;

import com.drcnet.highway.service.es.EsService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @Author jack
 * @Date: 2019/10/12 14:48
 * @Desc:
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class EsTest {

    @Resource
    private EsService esService;

    @Test
    public void testInsertData() {
        try {
            //esService.addData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSearch() {
        esService.search();
    }
}
