package com.drcnet.highway.service;

import com.drcnet.highway.dao.TietouMapper;
import com.drcnet.highway.dao.TietouFeatureScoreMapper;
import com.drcnet.highway.entity.TietouOrigin;
import com.drcnet.highway.entity.TietouFeatureScore;
import com.drcnet.highway.util.templates.BaseService;
import com.drcnet.highway.util.templates.MyMapper;
import com.drcnet.highway.vo.PageVo;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/10 13:27
 * @Description:
 */
@Service
public class TietoufeatureService implements BaseService<TietouFeatureScore, Integer> {

    @Resource
    private TietouFeatureScoreMapper thisMapper;

    @Override
    public MyMapper<TietouFeatureScore> getMapper() {
        return thisMapper;
    }


}
