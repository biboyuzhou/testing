package com.drcnet.highway.util;

import com.drcnet.highway.dto.request.MonthPageDto;
import tk.mybatis.mapper.entity.Example;

/**
 * @Author: penghao
 * @CreateTime: 2019/1/18 10:50
 * @Description:
 */
public class ServiceUtil {


    public static void setCretiriaPage(Example.Criteria criteria, MonthPageDto monthPageDto){
        if (monthPageDto.getBeginMonth().equals(monthPageDto.getEndMonth())){
            criteria.andEqualTo("monthTime",monthPageDto.getEndMonth());
        }else {
            criteria.andBetween("monthTime",monthPageDto.getBeginMonth(),monthPageDto.getEndMonth());
        }
    }

}
