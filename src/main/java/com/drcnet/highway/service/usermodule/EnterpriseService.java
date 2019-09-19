package com.drcnet.highway.service.usermodule;

import com.drcnet.highway.dao.EnterpriseMapper;
import com.drcnet.highway.dto.request.EnterpriseDto;
import com.drcnet.highway.entity.Enterprise;
import com.drcnet.highway.exception.MyException;
import com.drcnet.highway.util.templates.BaseService;
import com.drcnet.highway.util.templates.MyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/6 16:16
 * @Description:
 */
@Service
@Slf4j
public class EnterpriseService implements BaseService<Enterprise,Integer> {

    @Resource
    private EnterpriseMapper enterpriseMapper;

    @Override
    public MyMapper<Enterprise> getMapper() {
        return enterpriseMapper;
    }

    @Transactional
    public void insertOneEnterprise(EnterpriseDto enterpriseDto) {
        String name = enterpriseDto.getName();
        Enterprise enterprise = new Enterprise();
        enterprise.setName(name);
        enterprise.setUseFlag(true);
        Enterprise res = enterpriseMapper.selectOne(enterprise);
        if (res != null){
            throw new MyException("已有该企业，请勿重复添加");
        }
        enterprise.setCreateTime(LocalDateTime.now());
        enterpriseMapper.insertSelective(enterprise);
    }
}
