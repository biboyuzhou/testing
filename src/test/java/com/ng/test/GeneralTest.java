package com.drcnet.highway;

import org.junit.Test;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: penghao
 * @CreateTime: 2019/1/14 13:25
 * @Description:
 */
public class GeneralTest {

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

}
