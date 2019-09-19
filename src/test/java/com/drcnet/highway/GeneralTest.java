package com.drcnet.highway;

import com.drcnet.highway.dao.StationDicMapper;
import com.drcnet.highway.dao.TietouCarDicMapper;
import com.drcnet.highway.entity.dic.StationDic;
import com.drcnet.highway.entity.dic.TietouCarDic;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: penghao
 * @CreateTime: 2019/1/14 13:25
 * @Description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class GeneralTest {
    @Resource
    private StationDicMapper stationDicMapper;
    @Resource
    private TietouCarDicMapper tietouCarDicMapper;

    @Test
    public void generalTest(){

        Set<Integer> set1 = new TreeSet<>();
        Set<Integer> set2 = new TreeSet<>();
        Set<Integer> set3 = new TreeSet<>();
        Set<Integer> set4 = new TreeSet<>();
        set1.add(1);
        set2.add(1);
        set2.add(2);
        set3.add(1);
        set3.add(2);
        set3.add(3);
        set4.add(1);
        set4.add(2);
        set4.add(3);
        List<Set<Integer>> sets = new ArrayList<>();
        sets.add(set1);sets.add(set2);sets.add(set3);sets.add(set3);
        System.out.println(sets);
        Iterator<Set<Integer>> iterator = sets.iterator();
        while (iterator.hasNext()){
            Set<Integer> next = iterator.next();
            for (Set<Integer> re : sets) {
                if (!re.equals(next) && re.containsAll(next)){
                    iterator.remove();
                    break;
                }
            }
        }
        System.out.println(sets);
        Set<Set<Integer>> collect = sets.stream().distinct().collect(Collectors.toSet());
        System.out.println(collect);
    }

    @Test
    public void regExpTest(){
        System.out.println(StringUtils.isEmpty(" ".trim()));

    }

    @Test
    public void testInsertStation() {
        /*StationDic stationDic = new StationDic();
        stationDic.setStationName("测试站");
        int result = stationDicMapper.insertStationName(stationDic);*/


        TietouCarDic newCar = new TietouCarDic();
        newCar.setId(null);
        newCar.setCarNo("川AW5699");
        newCar.setUseFlag(true);
        newCar.setCreateTime(LocalDateTime.now());
        tietouCarDicMapper.insertNewCar(newCar);
        Integer id = newCar.getId();
        System.out.println(id);
    }

}
