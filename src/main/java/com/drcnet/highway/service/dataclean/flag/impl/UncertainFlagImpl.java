package com.drcnet.highway.service.dataclean.flag.impl;

import com.drcnet.highway.service.dataclean.flag.FlagChooseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;

/**
 * @Author jack
 * @Date: 2019/6/4 10:08
 * @Desc: 不确定标志异常处理service
 **/
@Slf4j
@Service("uncertainFlagService")
public class UncertainFlagImpl implements FlagChooseService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private BoundSetOperations<String, Object> boundSetOps;

    @Override
    public void putFlag2Cache(Set<Integer> idList) {
        boundSetOps  = redisTemplate.boundSetOps("flagDiff");
        if (!CollectionUtils.isEmpty(idList)) {
            for (Integer i : idList) {
                boundSetOps.add(i);
            }
        }
    }

    @Override
    public void processFlag(String flagstationinfo, String real, Set<Integer> uncertainFlagList, Map<String, String> flagMap, Integer id) {
        int length = flagstationinfo.length();
        int different = 0;
        int centain = 0;
        for (int i = 0; i < length; i += 3) {
            if ((length - i) < 3) {
                continue;
            }
            String flag = flagstationinfo.substring(i, i + 3);
            if (!real.contains(flag)) {
                different++;
                if (flagMap.containsKey(flag)) {
                    centain++;
                }
            }
        }
        //如果理论和实际差异的标志站数据都属于有问题的标志站，则将该条数据id存入缓存
        if (different > 0 && different == centain) {
            uncertainFlagList.add(id);
        }
    }
}
