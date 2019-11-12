package com.drcnet.highway.controller;

import com.drcnet.highway.annotation.DynamicDataSource;
import com.drcnet.highway.constants.ModuleConsts;
import com.drcnet.highway.constants.TipsConsts;
import com.drcnet.highway.dto.request.*;
import com.drcnet.highway.dto.response.LoginResponseDto;
import com.drcnet.highway.entity.Enterprise;
import com.drcnet.highway.entity.usermodule.Role;
import com.drcnet.highway.entity.usermodule.User;
import com.drcnet.highway.exception.MyException;
import com.drcnet.highway.service.usermodule.EnterpriseService;
import com.drcnet.highway.service.usermodule.RoleService;
import com.drcnet.highway.service.usermodule.UserService;
import com.drcnet.highway.util.Result;
import com.drcnet.highway.util.validate.AddValid;
import com.drcnet.highway.util.validate.PageValid;
import com.drcnet.highway.util.validate.UpdateValid;
import com.drcnet.usermodule.annotation.LoginCheck;
import com.drcnet.usermodule.annotation.PermissionCheck;
import com.drcnet.usermodule.permission.SecurityUtil;
import com.drcnet.usermodule.permission.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Author: penghao
 * @CreateTime: 2019/7/29 11:08
 * @Description:
 */
@RestController
@RequestMapping("user")
@Api(tags = "用户接口")
@Slf4j
public class UserController {

    @Value("${drcnet.security-switch}")
    private Boolean securitySwitch;


    @Resource
    private UserService userService;
    @Resource
    private RoleService roleService;
    @Resource
    private EnterpriseService enterpriseService;


    @ApiOperation("新增一个用户")
    @PostMapping("register")
    @PermissionCheck(ModuleConsts.ACCOUNT_MANAGEMENT)
    public Result register(@RequestBody @Validated(AddValid.class)UserDto userDto){
        userService.register(userDto);
        return Result.ok();
    }

    @ApiOperation("删除一个用户")
    @DeleteMapping("delete")
    @PermissionCheck(ModuleConsts.ACCOUNT_MANAGEMENT)
    public Result delete(@RequestParam Integer userId){
        //不能修改admin
        if (userId == 1){
            return Result.error("不能修改admin帐号的资料!");
        }
        User user = (User) UserContext.get();
        if (user.getId().equals(userId)) {
            return Result.error("不能删除自己");
        }
        User aim = userService.selectByPrimaryKey(userId);
        if (aim == null || !aim.getUseFlag()){
            return Result.error(TipsConsts.USER_NOT_FOUND);
        }
        userService.deleteOneUser(userId);
        return Result.ok();
    }


    @ApiOperation("修改用户信息")
    @PutMapping("updateUserMsg")
    @PermissionCheck(ModuleConsts.ACCOUNT_MANAGEMENT)
    public Result updateUserMsg(@RequestBody @Validated(UpdateValid.class) UserUpdateDto userDto){
        //不能修改admin
        if (userDto.getId() == 1){
            return Result.error("不能修改admin帐号的资料!");
        }

        Integer roleId = userDto.getRoleId();
        Integer enterpriseId = userDto.getEnterpriseId();

        if (roleId != null){
            Role role = roleService.selectByPrimaryKey(roleId);
            if (role == null || !role.getUseFlag()){
                return Result.error(TipsConsts.ROLE_NOT_FOUND);
            }
            //不能修改自己的角色
            User user = (User) UserContext.get();
            if (user.getId().equals(userDto.getId()) && !roleId.equals(user.getRoleId())){
                return Result.error(TipsConsts.CAN_NOT_UPDATE_MY_ROLE);
            }
        }
        Enterprise enterprise;
        if (enterpriseId != null && ((enterprise = enterpriseService.selectByPrimaryKey(enterpriseId)) == null || !enterprise.getUseFlag())) {
            throw new MyException(TipsConsts.ENTERPRISE_NOT_FOUND);
        }
        userService.updateUserMsg(userDto);
        return Result.ok();
    }

    @ApiOperation(value = "登陆",notes = "roleId:1管理员，2数据管理员，3普通用户")
    @PostMapping("login")
    public Result login(@RequestBody @Validated(AddValid.class) LoginDto loginDto){
        if (securitySwitch){
            String token = SecurityUtil.login(loginDto.getUsername(), loginDto.getPassword());
            User user = (User) UserContext.get();
            return Result.ok(new LoginResponseDto(token,user.getRoleId(),user.getPermissions()));
        }
        return Result.ok(new LoginResponseDto());
    }

    @ApiOperation("注销")
    @GetMapping("logOut")
    @LoginCheck
    public Result logOut(@RequestHeader String token){
        SecurityUtil.loginOut(token);
        return Result.ok();
    }

//    @ApiOperation("分配角色")
//    @PutMapping("allotRole")
//    @PermissionCheck(ModuleConsts.ACCOUNT_MANAGEMENT)
    public Result allotRole(@RequestBody @Validated(AddValid.class) AllotRoleDto allotRoleDto){
        Integer userId = allotRoleDto.getUserId();
        Integer roleId = allotRoleDto.getRoleId();
        User user = userService.selectByPrimaryKey(allotRoleDto.getUserId());
        if (user == null || !user.getUseFlag()){
            return Result.error(TipsConsts.USER_NOT_FOUND);
        }
        Role role = roleService.selectByPrimaryKey(allotRoleDto.getRoleId());
        if (role == null || !role.getUseFlag()){
            return Result.error(TipsConsts.ROLE_NOT_FOUND);
        }
        userService.allotRole(userId,roleId);
        return Result.ok();
    }

    @ApiOperation("获得用户列表")
    @GetMapping("listUsers")
    @PermissionCheck(ModuleConsts.ACCOUNT_MANAGEMENT)
    public Result listUsers(@Validated(PageValid.class) UserQueryDto queryDto){
        return Result.ok(userService.listUsers(queryDto));
    }


    @ApiOperation("测试")
    @GetMapping("test")
    @DynamicDataSource
    public Result test(){
        User user = userService.selectByBinaryUsername("ph");
        return Result.ok(user);
    }
}
