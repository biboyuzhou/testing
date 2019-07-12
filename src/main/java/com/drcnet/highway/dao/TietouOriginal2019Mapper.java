package com.drcnet.highway.dao;

import com.drcnet.highway.entity.TietouOriginal2019;
import com.drcnet.highway.util.templates.MyMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TietouOriginal2019Mapper extends MyMapper<TietouOriginal2019> {
    /**
     * 分批查询TietouOriginal2019表的理论标志站和实际标志站数据
     * @param begin
     * @param end
     * @return
     */
    List<TietouOriginal2019> listStationFlagByPeroid(@Param("begin") int begin, @Param("end") int end);

    /**
     * 查询TietouOriginal2019的最大值
     * @return
     */
    Integer selectMaxId();
}