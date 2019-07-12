package com.drcnet.highway.service.dataclean.flag;

import java.util.Map;
import java.util.Set;

/**
 * @Author jack
 * @Date: 2019/6/4 10:02
 * @Desc: 标志站数据处理接口
 **/
public interface FlagChooseService {

    /**
     * 将筛选出的数据id放入缓存
     * @param idList
     */
    void putFlag2Cache(Set<Integer> idList);


    /**
     * 对理论标志站和实际标志站数据进行处理
     * @param flagstationinfo
     * @param real
     * @param certainFlagList
     * @param flagMap
     * @param id
     */
    void processFlag(String flagstationinfo, String real, Set<Integer> certainFlagList, Map<String, String> flagMap, Integer id);
}
