package com.drcnet.highway.dao;

import com.drcnet.highway.dto.request.UserQueryDto;
import com.drcnet.highway.entity.usermodule.User;
import com.drcnet.highway.util.templates.MyMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper extends MyMapper<User> {
    User selectByBinaryUsername(@Param("username") String username);

    List<String> listPermissionsByUserId(@Param("roleId") Integer roleId);

    List<User> listUsers(UserQueryDto queryDto);
}