package com.drcnet.highway.controller;

import com.drcnet.highway.dto.request.EnterpriseDto;
import com.drcnet.highway.service.usermodule.EnterpriseService;
import com.drcnet.highway.util.Result;
import com.drcnet.highway.util.validate.AddValid;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/6 16:17
 * @Description:
 */
@Api(tags = "企业接口")
@RestController
@RequestMapping("enterprise")
@Slf4j
public class EnterpriseController {

    @Resource
    private EnterpriseService enterpriseService;

    @ApiOperation("新增一个企业")
    @PostMapping("addOneEnterprise")
    public Result addOneEnterprise(@RequestBody @Validated(AddValid.class) EnterpriseDto enterpriseDto){
        enterpriseService.insertOneEnterprise(enterpriseDto);
        return Result.ok();
    }

}
