package com.drcnet.highway.permission;

import com.drcnet.highway.constants.TipsConsts;
import com.drcnet.highway.entity.usermodule.User;
import com.drcnet.highway.exception.MyException;
import com.drcnet.highway.service.usermodule.UserService;
import com.drcnet.highway.util.AuthenticationUtil;
import com.drcnet.usermodule.permission.AuthcRealm;
import com.drcnet.usermodule.permission.UserContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @Author: penghao
 * @CreateTime: 2019/4/3 13:27
 * @Description:
 */
@Component
public class MyRealm implements AuthcRealm {

    @Resource
    private UserService userService;

    private List<String> blankList = new ArrayList<>();

    @Override
    public Serializable doGetAuthentication(String username, String password) {
        User user = userService.selectByBinaryUsername(username);
        if (user == null || !user.getUseFlag()){
            throw new MyException(TipsConsts.USER_NOT_FOUND);
        }
        String encryptPassword = AuthenticationUtil.encryptPassword(password, user.getSalt());
        if (!user.getPassword().equals(encryptPassword)){
            throw new MyException(TipsConsts.PASSWORD_ERROR);
        }
        Integer roleId = user.getRoleId();
        if (roleId != null){
            List<String> permissions = userService.getPermissions(roleId);
            user.setPermissions(permissions);
        }
        return user;
    }

    @Override
    public Collection<String> doGetAuthorize() {
        if (!UserContext.isAuthenticate()) {
            return blankList;
        }
        User user = (User)UserContext.get();
        List<String> permissions = user.getPermissions();
        if (permissions == null)
            permissions = blankList;
        return permissions;
    }
}
