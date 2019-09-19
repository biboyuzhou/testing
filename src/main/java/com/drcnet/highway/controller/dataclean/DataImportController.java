package com.drcnet.highway.controller.dataclean;

import com.drcnet.highway.constants.ModuleConsts;
import com.drcnet.highway.enums.BoundEnum;
import com.drcnet.highway.service.dataclean.DataImportService;
import com.drcnet.highway.util.DownloadUtil;
import com.drcnet.highway.util.Result;
import com.drcnet.response.consts.TipsConsts;
import com.drcnet.usermodule.annotation.ParamToken;
import com.drcnet.usermodule.annotation.PermissionCheck;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * @Author: penghao
 * @CreateTime: 2019/7/23 10:35
 * @Description:
 */
@RequestMapping("dataImport")
@Slf4j
@RestController
@Api(tags = "数据导入接口")
public class DataImportController {

    @Resource
    private DataImportService dataImportService;

    @ApiOperation(value = "导入数据", notes = "type:0入口数据，1出口数据")
    @PostMapping("importOutboundByExcel/{type}")
    @PermissionCheck(ModuleConsts.UPLOAD_DATA)
    public Result importOutboundByExcel(@RequestPart MultipartFile file, @PathVariable Integer type) {
        if (file == null || file.isEmpty()) {
            return Result.error("请上传文件");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xls") && !filename.endsWith(".xlsx"))) {
            return Result.error("文件格式异常");
        }
        try {
            dataImportService.uploadFile(file, type);
        } catch (IOException e) {
            log.error("{}", e);
            return Result.error(TipsConsts.UPLOAD_FAILED);
        }
        return Result.ok();
    }

    @ApiOperation(value = "模板excel下载", notes = "type:0入口数据模板，1出口数据模板")
    @GetMapping("downloadTemplateExcel/{type}")
    @PermissionCheck(ModuleConsts.UPLOAD_DATA)
    @ParamToken
    public ResponseEntity<byte[]> downloadTemplateExcel(@PathVariable Integer type) {
        String fileName;
        if (type == BoundEnum.INBOUND.code) {
            fileName = "入口数据模板.xls";
        } else if (type == BoundEnum.OUTBOUND.code) {
            fileName = "出口数据模板.xls";
        } else {
            return ResponseEntity.ok(TipsConsts.SERVER_ERROR.getBytes(Charset.forName("utf8")));
        }
        ClassPathResource resource = new ClassPathResource("static/" + fileName);
        try {
            try (InputStream is = resource.getInputStream()) {
                return DownloadUtil.download(is, fileName);
            }
        } catch (IOException e) {
            log.error("{}", e);
        }
        return ResponseEntity.ok(TipsConsts.SERVER_ERROR.getBytes(Charset.forName("utf8")));
    }


    @ApiOperation("导入二绕入口数据")
    @GetMapping("importInboundDataByExcel")
    public Result importInboundDataByExcel() {
        long timeMillis = System.currentTimeMillis();
        log.info("开始导入入口数据");
        dataImportService.importInboundDataByExcel();
        log.info("数据导入成功，耗时:{} 秒", (System.currentTimeMillis() - timeMillis) / 1000);
        return Result.ok();
    }

    @ApiOperation("导入txt文本中的原始数据")
    @GetMapping("importTxtOriginalData")
    public Result importTxtOriginalData(){
        dataImportService.importTxtOriginalData(",",new File("D:\\备份\\铁投6-8UTF8.txt"));
        return Result.ok();
    }


    @ApiOperation("导入二绕7月份的数据")
    @GetMapping("importByExcel")
    public Result importByExcel(){
        long timeMillis = System.currentTimeMillis();
        log.info("开始导入数据");
        dataImportService.importNewData();
        log.info("数据导入成功，耗时:{} 秒",(System.currentTimeMillis() - timeMillis)/1000);
        return Result.ok();
    }

}
