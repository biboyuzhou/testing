package com.drcnet.highway.service;

import com.drcnet.highway.dao.TietouCarDicMapper;
import com.drcnet.highway.dao.TietouInboundMapper;
import com.drcnet.highway.dto.request.ChangeCardQueryDto;
import com.drcnet.highway.dto.response.ChangeCardResponse;
import com.drcnet.highway.entity.TietouInbound;
import com.drcnet.highway.entity.dic.TietouCarDic;
import com.drcnet.highway.util.DateUtils;
import com.drcnet.highway.util.templates.BaseService;
import com.drcnet.highway.util.templates.MyMapper;
import com.drcnet.highway.vo.PageVo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.List;

/**
 * @Author jack
 * @Date: 2019/8/2 10:12
 * @Desc:
 **/
@Service
@Slf4j
public class TietouInboundService implements BaseService<TietouInbound, Integer> {
    @Resource
    private TietouInboundMapper tietouInboundMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private TietouCarDicMapper tietouCarDicMapper;

    @Override
    public MyMapper<TietouInbound> getMapper() {
        return tietouInboundMapper;
    }

    /**
     * 获取换卡风险数据
     * @param dto
     * @return
     */
    public PageVo<ChangeCardResponse> getChangeCardList(ChangeCardQueryDto dto) throws ParseException {
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps("car_cache");

        Integer envlpId = null;
        if (!StringUtils.isEmpty(dto.getInCarNo())) {
            envlpId = (Integer) hashOperations.get(dto.getInCarNo());
            if (envlpId == null) {
                envlpId = getCarNoIdFromDb(dto.getInCarNo());
            }
            dto.setEnvlpId(envlpId == null ? -1 : envlpId);
        }

        Integer vlpId = null;
        if (!StringUtils.isEmpty(dto.getOutCarNo())) {
            vlpId = (Integer) hashOperations.get(dto.getOutCarNo());
            if (vlpId == null) {
                vlpId = getCarNoIdFromDb(dto.getOutCarNo());
            }
            //防止输入异常字符时缓存和库均没有，vlpId为null从而不加入筛选条件
            dto.setVlpId(vlpId == null ? -1 : vlpId);
        }

        String startTime = "";
        String endTime = "";
        if (StringUtils.isEmpty(dto.getBeginDate())) {
            startTime = new StringBuilder(DateUtils.getFirstDayOfCurrentYear()).append(" 00:00:00").toString();
        } else {
            startTime = new StringBuilder(DateUtils.convertDatePattern(dto.getBeginDate())).append(" 00:00:00").toString();
        }
        if (StringUtils.isEmpty(dto.getEndDate())) {
            endTime = new StringBuilder(DateUtils.getCurrentDay()).append(" 23:59:59").toString();
        } else {
            endTime = new StringBuilder(DateUtils.convertDatePattern(dto.getEndDate())).append(" 00:00:00").toString();
        }

        dto.setBeginDate(startTime);
        dto.setEndDate(endTime);

        PageHelper.startPage(dto.getPageNum(),dto.getPageSize());
        List<ChangeCardResponse> changeCardResponseList = tietouInboundMapper.listChangeCardList(dto);
        return PageVo.of(changeCardResponseList);
    }

    private Integer getCarNoIdFromDb(String carNo) {
        TietouCarDic carDic = tietouCarDicMapper.getIdByCarNo(carNo);
        if (carDic != null) {
            return carDic.getId();
        }
        return null;
    }


    public int updateConfirmState(Integer enId, Integer state) {
        TietouInbound tietouInbound = new TietouInbound();
        tietouInbound.setId(enId);
        tietouInbound.setChangeCardConfirm(state);
        return tietouInboundMapper.updateByPrimaryKeySelective(tietouInbound);
    }
}
