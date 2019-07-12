package com.drcnet.highway.controller;

import com.drcnet.highway.constants.TipsConsts;
import com.drcnet.highway.dto.SuccessAmountDto;
import com.drcnet.highway.dto.request.BlackDetailQueryDto;
import com.drcnet.highway.dto.request.BlackListInsertDto;
import com.drcnet.highway.dto.request.DetailMarkDto;
import com.drcnet.highway.dto.request.WhiteDto;
import com.drcnet.highway.entity.TietouBlacklist;
import com.drcnet.highway.entity.dic.TietouCarDic;
import com.drcnet.highway.entity.TietouOrigin;
import com.drcnet.highway.service.TietouBlackListService;
import com.drcnet.highway.service.TietouService;
import com.drcnet.highway.service.dic.TietouCarDicService;
import com.drcnet.highway.util.EntityUtil;
import com.drcnet.highway.util.Result;
import com.drcnet.highway.util.validate.AddValid;
import com.drcnet.highway.util.validate.PageValid;
import com.drcnet.highway.vo.PageVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/10 10:04
 * @Description:
 */
@RestController
@RequestMapping("blackwhite")
@Api(value = "WhiteAndBlackController",tags = "黑白名单接口")
public class WhiteAndBlackController {

    @Resource
    private TietouCarDicService tietouCarDicService;
    @Resource
    private TietouBlackListService tietouBlackListService;
    @Resource
    private TietouService tietouService;

    @ApiOperation(value = "将车牌加入或取消白名单",notes = "flag = 1 为加入白名单，0为取消白名单")
    @PutMapping("addWhiteList")
    @Transactional
    public Result addWhiteList(@RequestBody @Validated List<WhiteDto> whiteDtos){
        for (WhiteDto whiteDto : whiteDtos) {
            if (StringUtils.isEmpty(whiteDto.getCarNo()))
                return Result.error("车牌号不能为空");
            tietouCarDicService.addWhiteList(whiteDto.getCarNo(),whiteDto.getFlag());
        }
        return Result.ok();
    }

    @ApiOperation(value = "查询车辆白名单列表")
    @GetMapping("listWhite")
    public Result listWhite(@RequestParam Integer pageNum,@RequestParam Integer pageSize){
        PageVo<TietouCarDic> pageVo = tietouCarDicService.listWhite(pageNum, pageSize);
        return Result.ok(pageVo);
    }

    @Transactional
    @PostMapping("addOrCancelBlackList")
    @ApiOperation(value = "添加或取消黑名单",notes = "flag:1为添加，0为取消")
    public Result addOrCancelBlackList(@RequestBody @Validated(AddValid.class) List<BlackListInsertDto> dtos){
        for (BlackListInsertDto dto : dtos) {
            if (StringUtils.isEmpty(dto.getCarNo()))
                return Result.error("车牌号不能为空");
            if (dto.getFlag() == 1 && (dto.getCheating() == null || dto.getScore()==null||dto.getViolation()==null))
                return Result.error("三项得分不能为空");
            tietouBlackListService.addOrCancelBlackList(dto);
        }
        return Result.ok();
    }

    @ApiOperation(value = "查询车辆黑名单列表")
    @GetMapping("listBlack")
    public Result listBlack(@RequestParam Integer pageNum,@RequestParam Integer pageSize){
        PageVo<TietouBlacklist> pageVo = tietouBlackListService.listBlack(pageNum, pageSize);
        return Result.ok(pageVo);
    }

    @GetMapping("listBlackListDetail")
    @ApiOperation("查询黑名单通行记录详细列表")
    public Result listBlackListDetail(@Validated({PageValid.class}) BlackDetailQueryDto queryDto){
        PageVo<TietouOrigin> pageVo = tietouBlackListService.listBlackListDetail(queryDto);
        return Result.ok(pageVo);
    }

    @PutMapping("markDetailList")
    @ApiOperation(value = "标记通行记录",notes = "flag:true为标记，false为取消标记")
    public Result markDetailList(@RequestBody @Validated(AddValid.class) DetailMarkDto markDto){
        EntityUtil.dateMonthChecked(markDto.getMonthTime());
        tietouService.markDetailList(markDto);
        return Result.ok();
    }

    @PostMapping("uploadWhiteList")
    @ApiOperation("上传excel文件至白名单")
    public Result uploadWhiteList(@RequestPart MultipartFile file){
        if (file == null || file.isEmpty())
            return Result.error(TipsConsts.LACK_PARAMS);
        SuccessAmountDto amountDto = tietouCarDicService.uploadWhiteList(file);
        return Result.ok(amountDto);
    }

    @PostMapping("uploadBlackList")
    @ApiOperation("上传excel文件至黑名单")
    public Result uploadBlackList(@RequestPart MultipartFile file){
        if (file == null || file.isEmpty())
            return Result.error(TipsConsts.LACK_PARAMS);
        SuccessAmountDto amountDto = tietouCarDicService.uploadBlackList(file);
        return Result.ok(amountDto);
    }
}
