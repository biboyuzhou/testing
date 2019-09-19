package com.drcnet.highway.service.usermodule;

import com.drcnet.highway.constants.TipsConsts;
import com.drcnet.highway.dao.EnterpriseMapper;
import com.drcnet.highway.dao.UserMapper;
import com.drcnet.highway.dto.request.UserDto;
import com.drcnet.highway.dto.request.UserQueryDto;
import com.drcnet.highway.dto.request.UserUpdateDto;
import com.drcnet.highway.entity.Enterprise;
import com.drcnet.highway.entity.usermodule.User;
import com.drcnet.highway.exception.MyException;
import com.drcnet.highway.util.AuthenticationUtil;
import com.drcnet.highway.util.EntityUtil;
import com.drcnet.highway.util.templates.BaseService;
import com.drcnet.highway.util.templates.MyMapper;
import com.drcnet.highway.vo.PageVo;
import com.drcnet.usermodule.permission.SecurityUtil;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: penghao
 * @CreateTime: 2019/7/29 11:09
 * @Description:
 */
@Service
@Slf4j
public class UserService implements BaseService<User, Integer> {

    @Resource
    private UserMapper thisMapper;
    @Resource
    private EnterpriseMapper enterpriseMapper;

    @Override
    public MyMapper<User> getMapper() {
        return thisMapper;
    }

    /**
     * 获得用户的权限字符串集合
     */
    public List<String> getPermissions(Integer roleId) {
        return thisMapper.listPermissionsByUserId(roleId);
    }


    /**
     * 检查，企业是否存在，用户名是否重复
     */
    private void registerChecked(User user) {
        String username = user.getUsername();
        Integer enterpriseId = user.getEnterpriseId();
        if (enterpriseId != null) {
            Enterprise enterprise = enterpriseMapper.selectByPrimaryKey(enterpriseId);
            if (enterprise == null || !enterprise.getUseFlag()) {
                throw new MyException("企业不存在或企业不可用");
            }
        }
        User res = thisMapper.selectByBinaryUsername(username);
        if (res != null) {
            throw new MyException("用户名已存在");
        }
    }

    /**
     * 根据用户名查询
     */
    public User selectByBinaryUsername(String username) {
        return thisMapper.selectByBinaryUsername(username);
    }

    /**
     * 分配角色
     */
    @Transactional
    public void allotRole(Integer userId, Integer roleId) {
        User user = new User();
        user.setId(userId);
        user.setRoleId(roleId);
        thisMapper.updateByPrimaryKeySelective(user);
    }

    /**
     * 新增一个用户
     */
    @Transactional
    public void register(UserDto userDto) {
        User user = EntityUtil.copyNotNullFields(userDto, new User());
        String password = user.getPassword();
        //检查，企业是否存在，用户名是否重复
        registerChecked(user);
        String newSalt = AuthenticationUtil.generateSalt();
        String completePassword = AuthenticationUtil.encryptPassword(password, newSalt);
        user.setPassword(completePassword);
        user.setSalt(newSalt);
        user.setCreateTime(LocalDateTime.now());
        user.setUseFlag(true);
        thisMapper.insertSelective(user);
    }

    /**
     * 修改用户信息
     *
     * @param userDto 用户信息载体
     */
    @Transactional
    public void updateUserMsg(UserUpdateDto userDto) {
        Integer id = userDto.getId();
        User userDB = thisMapper.selectByPrimaryKey(id);
        if (userDB == null || !userDB.getUseFlag()) {
            throw new MyException(TipsConsts.USER_NOT_FOUND);
        }
        String password = userDto.getPassword();
        User user = EntityUtil.copyNotNullFields(userDto, new User());
        if (!StringUtils.isBlank(password)) {
            //生成新的盐
            String newSalt = AuthenticationUtil.generateSalt();
            String encryptPassword = AuthenticationUtil.encryptPassword(password, newSalt);
            user.setPassword(encryptPassword);
            user.setSalt(newSalt);
        } else {
            user.setPassword(null);
        }
        Integer roleId = userDto.getRoleId();
        Integer roleIdOld = userDB.getRoleId();
        thisMapper.updateByPrimaryKeySelective(user);
        if (roleId != null && !roleId.equals(roleIdOld) || !StringUtils.isBlank(password)){
            //登陆下线
            SecurityUtil.loginOutById(id);
        }
    }

    @Transactional
    public void deleteOneUser(Integer userId) {
        User user = new User();
        user.setId(userId);
        user.setUseFlag(false);
        thisMapper.updateByPrimaryKeySelective(user);
        //将已登陆的该用户强制登出
        SecurityUtil.loginOutById(userId);
    }

    /**
     * 查询用户列表
     *
     * @param queryDto
     */
    public PageVo<User> listUsers(UserQueryDto queryDto) {
        PageHelper.startPage(queryDto.getPageNum(), queryDto.getPageSize());
        queryDto.setUsername(EntityUtil.formatKeyWordWithPrev(queryDto.getUsername()));
        queryDto.setName(EntityUtil.formatKeyWordWithPrev(queryDto.getName()));
        List<User> users = thisMapper.listUsers(queryDto);
        return PageVo.of(users);
    }
}
