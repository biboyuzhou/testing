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
 * @Date: 2019/6/4 10:07
 * @Desc: 确定标志异常处理service
 **/
@Slf4j
@Service("certainFlagService")
public class CertainFlagImpl implements FlagChooseService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private BoundSetOperations<String, Object> boundSetOps;

    @Override
    public void putFlag2Cache(Set<Integer> idList) {
        boundSetOps  = redisTemplate.boundSetOps("flagLost");
        if (!CollectionUtils.isEmpty(idList)) {
            for (Integer i : idList) {
                boundSetOps.add(i);
            }

        }

    }

    @Override
    public void processFlag(String flagstationinfo, String real, Set<Integer> certainFlagList, Map<String, String> flagMap, Integer id) {
        int length = flagstationinfo.length();
        int count = 0;
        int certain = 0;
        for (int i = 0; i < length; i += 3) {
            if ((length - i) < 3) {
                continue;
            }
            count++;
            String flag = flagstationinfo.substring(i, i + 3);
            if (flagMap.containsKey(flag)) {
                certain++;
            }
        }

        //如果确定有问题的标志站次数等于总的循环次数，则说明该条记录所有的理论标注站都是属于有问题的标志站，需将id存入缓存
        if (count > 0 && count == certain) {
            certainFlagList.add(id);
        }
    }
}
