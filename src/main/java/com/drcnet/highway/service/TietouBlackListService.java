package com.drcnet.highway.service;

import com.drcnet.highway.constants.TipsConsts;
import com.drcnet.highway.dao.TietouBlacklistMapper;
import com.drcnet.highway.dao.TietouCarDicMapper;
import com.drcnet.highway.dto.request.BlackDetailQueryDto;
import com.drcnet.highway.dto.request.BlackListInsertDto;
import com.drcnet.highway.entity.TietouBlacklist;
import com.drcnet.highway.entity.dic.TietouCarDic;
import com.drcnet.highway.entity.TietouOrigin;
import com.drcnet.highway.exception.MyException;
import com.drcnet.highway.util.EntityUtil;
import com.drcnet.highway.util.templates.BaseService;
import com.drcnet.highway.util.templates.MyMapper;
import com.drcnet.highway.vo.PageVo;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/13 14:07
 * @Description:
 */
@Service
public class TietouBlackListService implements BaseService<TietouBlacklist, Integer> {

    @Resource
    private TietouBlacklistMapper thisMapper;
    @Resource
    private TietouCarDicMapper tietouCarDicMapper;
    @Resource
    private TietouService tietouService;

    @Override
    public MyMapper<TietouBlacklist> getMapper() {
        return thisMapper;
    }

    /**
     * 添加或删除一个黑名单
     */
    public void addOrCancelBlackList(BlackListInsertDto dto) {
        TietouCarDic query = new TietouCarDic();
        query.setCarNo(dto.getCarNo());
        TietouCarDic tietouCarDic = tietouCarDicMapper.selectOne(query);
        if (dto.getFlag() == 0 && tietouCarDic == null) {
            throw new MyException("没有该车牌的记录!");
        } else if (tietouCarDic == null) {
            tietouCarDic = query;
            tietouCarDic.setWhiteFlag(false);
            tietouCarDic.setUseFlag(true);
            tietouCarDic.setCreateTime(LocalDateTime.now());
            tietouCarDicMapper.insertSelective(tietouCarDic);
        }
        TietouBlacklist blackQuery = new TietouBlacklist();
        blackQuery.setCarNoId(tietouCarDic.getId());
        TietouBlacklist blacklist = thisMapper.selectOne(blackQuery);

        if (dto.getFlag() == 1) {
            if (blacklist != null) {
                TietouBlacklist update = EntityUtil.copyNotNullFields(dto, new TietouBlacklist());
                update.setId(blacklist.getId());
                update.setUpdateTime(LocalDateTime.now());
                update.setUseFlag(true);
                thisMapper.updateByPrimaryKeySelective(update);
            } else {
                blacklist = blackQuery;
                EntityUtil.copyNotNullFields(dto, blacklist);
                blacklist.setCreateTime(LocalDateTime.now());
                blacklist.setUseFlag(true);
                thisMapper.insertSelective(blacklist);
            }
            //如果车牌为白名单，则将其排除出白名单
            if (tietouCarDic.getWhiteFlag()) {
                tietouCarDic.setWhiteFlag(false);
                tietouCarDicMapper.updateByPrimaryKeySelective(tietouCarDic);
            }
        } else if (dto.getFlag() == 0) {
            if (blacklist == null || !blacklist.getUseFlag()) {
                throw new MyException("该车牌未被列入黑名单");
            }
            TietouBlacklist update = new TietouBlacklist();
            update.setId(blacklist.getId());
            update.setUseFlag(false);
            update.setUpdateTime(LocalDateTime.now());
            thisMapper.updateByPrimaryKeySelective(update);
        } else {
            throw new MyException(TipsConsts.PARAMS_ERROR);
        }
    }

    /**
     * 查询黑名单列表
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageVo<TietouBlacklist> listBlack(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize, "create_time desc");
        TietouBlacklist query = new TietouBlacklist();
        query.setUseFlag(true);
        List<TietouBlacklist> blacklists = thisMapper.select(query);
        return PageVo.of(blacklists);
    }

    /**
     * 查询黑名单车辆的详细通行信息
     *
     * @param queryDto 查询条件
     */
    public PageVo<TietouOrigin> listBlackListDetail(BlackDetailQueryDto queryDto) {
        Integer carNoId = null;
        if (queryDto.getCarNo() != null){
            TietouBlacklist res = queryByCarNo(queryDto.getCarNo());
            if (res == null) {
                throw new MyException(TipsConsts.BLACK_NOT_FOUND);
            }
            carNoId = res.getCarNoId();
        }

        return tietouService.listDetailFromAllTimes(queryDto, carNoId);
    }

    /**
     * 通过车牌号查询
     *
     * @param carNo 车牌号
     */
    public TietouBlacklist queryByCarNo(String carNo) {
        TietouBlacklist query = new TietouBlacklist();
        query.setUseFlag(true);
        query.setCarNo(carNo);
        return thisMapper.selectOne(query);
    }

    /**
     * 通过车牌ID查询
     *
     * @param carId 车牌ID
     */
    public TietouBlacklist queryByCarNoId(Integer carId) {
        TietouBlacklist query = new TietouBlacklist();
        query.setUseFlag(true);
        query.setCarNoId(carId);
        return thisMapper.selectOne(query);
    }

    /**
     * 取消黑名单
     *
     * @param carNoId
     */
    public void cancelBlack(Integer carNoId) {
        TietouBlacklist res = queryByCarNoId(carNoId);
        if (res != null) {
            res.setUseFlag(false);
            res.setUpdateTime(LocalDateTime.now());
            thisMapper.updateByPrimaryKeySelective(res);
        }
    }
}
