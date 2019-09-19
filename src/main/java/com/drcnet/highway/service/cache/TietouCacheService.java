package com.drcnet.highway.service.cache;

import com.drcnet.highway.dao.TietouMapper;
import com.drcnet.highway.entity.TietouOrigin;
import com.drcnet.highway.util.domain.CarNoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/13 14:11
 * @Description:
 */
@Service
@Slf4j
public class TietouCacheService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private TietouMapper tietouMapper;

    private final String ORIGINAL_KEY_PREV = "originalDataUnique:";

    /**
     * 按时间将原始数据表的记录存至redis
     */
    public void initCache(LocalDate beginDate, LocalDate endDate) {
        List<TietouOrigin> tietouOrigins = tietouMapper.listByTimePeriod(beginDate, endDate);
        Map<LocalDate, Set<String>> dataMap = new HashMap<>(32);
        for (TietouOrigin tietouOrigin : tietouOrigins) {
            LocalDate localDate = toLocalDate(tietouOrigin.getExtime());
            Set<String> keySet = dataMap.computeIfAbsent(localDate, var -> new HashSet<>());
            keySet.add(CarNoUtil.formatOrigin(tietouOrigin));
        }
        for (Map.Entry<LocalDate, Set<String>> entry : dataMap.entrySet()) {
            LocalDate key = entry.getKey();
            Set<String> value = entry.getValue();
            BoundSetOperations<String, Object> setOps = redisTemplate.boundSetOps(ORIGINAL_KEY_PREV + key);
            //新增入库
            value.forEach(setOps::add);
        }
    }

    public LocalDate toLocalDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.toLocalDate();
    }
}
