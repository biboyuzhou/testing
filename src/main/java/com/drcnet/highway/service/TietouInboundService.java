package com.drcnet.highway.service;

import com.drcnet.highway.dao.TietouInboundMapper;
import com.drcnet.highway.entity.TietouInbound;
import com.drcnet.highway.util.templates.BaseService;
import com.drcnet.highway.util.templates.MyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

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

    @Override
    public MyMapper<TietouInbound> getMapper() {
        return tietouInboundMapper;
    }


}
