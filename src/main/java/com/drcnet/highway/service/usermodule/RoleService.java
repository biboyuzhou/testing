package com.drcnet.highway.service.usermodule;

import com.drcnet.highway.dao.RoleMapper;
import com.drcnet.highway.entity.usermodule.Role;
import com.drcnet.highway.util.templates.BaseService;
import com.drcnet.highway.util.templates.MyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/7 16:49
 * @Description:
 */
@Service
@Slf4j
public class RoleService implements BaseService<Role,Integer> {

    @Resource
    private RoleMapper roleMapper;

    @Override
    public MyMapper<Role> getMapper() {
        return roleMapper;
    }
}
