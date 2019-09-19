package com.drcnet.highway.service;

import com.drcnet.highway.constants.enumtype.YesNoEnum;
import com.drcnet.highway.dao.TietouMapper;
import com.drcnet.highway.dao.TietouSameStationFrequentlyMapper;
import com.drcnet.highway.dto.request.RiskInOutDto;
import com.drcnet.highway.entity.TietouOrigin;
import com.drcnet.highway.entity.TietouSameStationFrequently;
import com.drcnet.highway.util.templates.BaseService;
import com.drcnet.highway.util.templates.MyMapper;
import com.drcnet.highway.vo.PageVo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/20 14:52
 * @Description:
 */
@Service
@Slf4j
public class TietouSameStationFrequentlyService implements BaseService<TietouSameStationFrequently, Integer> {

    @Resource
    private TietouSameStationFrequentlyMapper thisMapper;
    @Resource
    private TietouMapper tietouMapper;

    @Override
    public MyMapper<TietouSameStationFrequently> getMapper() {
        return thisMapper;
    }


    public PageVo<TietouSameStationFrequently> listByQuery(RiskInOutDto riskInOutDto) {
        PageHelper.startPage(riskInOutDto.getPageNum(), riskInOutDto.getPageSize(), "out_time desc");
        List<TietouSameStationFrequently> select ;
        if (riskInOutDto.getIsCurrent().equals(YesNoEnum.YES.getCode())) {
            select = thisMapper.selectByTimeAndDistance(riskInOutDto);
        } else {
            select = thisMapper.selectByTimeAndDistanceFromAll(riskInOutDto);
        }

        return PageVo.of(select);
    }

    /**
     * 判断数据库里是否有对应时间的数据
     *
     * @param monthTime
     * @return
     */
    public boolean hasMonthTime(int monthTime) {
        thisMapper.hasMonthTime(monthTime);
        return false;
    }

    /**
     * 设置先出后进表内的附加数据(通行距离，时间，载重等)
     */
    @Transactional
    public void setInOutFrequencyExt() {
        List<TietouSameStationFrequently> res = thisMapper.selectAll();
        int i = 0;
        for (TietouSameStationFrequently re : res) {
            Integer outId = re.getOutId();
            Integer inId = re.getInId();
            setExtField(re, outId, inId);
            thisMapper.updateByPrimaryKeySelective(re);
            if (++i % 1000 == 0) {
                log.info("先出后进已更新完:{}条数据", i);
            }
        }
        log.info("先出后进附加信息更新完成!");
    }

    public void setExtField(TietouSameStationFrequently re, Integer outId, Integer inId) {
        TietouOrigin lastRecord = tietouMapper.selectByPrimaryKey(outId);
        TietouOrigin nextRecord = tietouMapper.selectByPrimaryKey(inId);
        if (lastRecord != null) {
            re.setLastDistance(lastRecord.getTolldistance());
            re.setLastWeight(lastRecord.getTotalweight());
            re.setLastInStationId(lastRecord.getRkId());
            re.setLastInStationName(lastRecord.getRk());
            re.setLastEntime(lastRecord.getEntime());
        }
        if (nextRecord != null) {
            re.setNextDistance(nextRecord.getTolldistance());
            re.setNextWeight(nextRecord.getTotalweight());
            re.setNextOutStationId(nextRecord.getCkId());
            re.setNextOutStationName(nextRecord.getCk());
            re.setNextExtime(nextRecord.getExtime());
        }
    }

    /**
     * 拉取SameStationFrequently data
     * @param stationIdList
     */
    public void pullSameStationFrequentlyFromAll(List<Integer> stationIdList) {
        thisMapper.pullSameStationFrequentlyFromAll(stationIdList);
    }

    public void truncate() {
        thisMapper.truncate();
    }
}
