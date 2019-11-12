package com.drcnet.highway;

import com.drcnet.highway.config.LocalVariableConfig;
import com.drcnet.highway.dao.TietouCarDicMapper;
import com.drcnet.highway.entity.dic.TietouCarDic;
import com.drcnet.highway.service.TaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @Author jack
 * @Date: 2019/8/7 13:59
 * @Desc:
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class LocalVariableTest {
    @Resource
    private LocalVariableConfig localVariableConfig;

    @Resource
    private TietouCarDicMapper carDicMapper;
    @Resource
    private TaskService taskService;

    @Test
    public void test() {

        System.out.println(localVariableConfig);
    }

    @Test
    public void testGetCar() {
        Integer vlpId = 887;
        TietouCarDic carDic = carDicMapper.selectByPrimaryKey(vlpId);
        System.out.println(carDic.getWeightMax());
    }

    @Test
    public void testTransaction() {
        Integer id = 4;
        try {
            taskService.getNewTaskId(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
