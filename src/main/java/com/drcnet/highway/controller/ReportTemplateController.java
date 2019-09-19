package com.drcnet.highway.controller;

import com.drcnet.highway.constants.ModuleConsts;
import com.drcnet.highway.constants.TipsConsts;
import com.drcnet.highway.dao.TietouCarDicMapper;
import com.drcnet.highway.dto.request.CheatingListTimeSearchDto;
import com.drcnet.highway.entity.TietouFeatureStatisticGyh;
import com.drcnet.highway.exception.MyException;
import com.drcnet.highway.service.ReportTemplateService;
import com.drcnet.highway.service.TietouScoreGyhService;
import com.drcnet.highway.util.DownloadUtil;
import com.drcnet.highway.util.Result;
import com.drcnet.highway.util.validate.QueryValid;
import com.drcnet.highway.vo.PageVo;
import com.drcnet.usermodule.annotation.ParamToken;
import com.drcnet.usermodule.annotation.PermissionCheck;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/1 10:19
 * @Description:
 */
@RestController
@RequestMapping("reportTemplate")
@Api(tags = "报告模板接口")
@Slf4j
public class ReportTemplateController {

    @Resource
    private ReportTemplateService reportTemplateService;
    @Resource
    private TietouCarDicMapper carDicMapper;
    @Resource
    private TietouScoreGyhService tietouScoreGyhService;

//    @GetMapping("buildReport")
//    @ApiOperation("生成异常车辆报告")
    public Result buildReport(){
//        List<String> carNoList = Arrays.asList("川AAQ119_货", "渝F878Q7", "川A72MC1", "川AM28D4","皖K66P98","川A7HQ44","川U21540","川A6773U_货");
//        List<TietouCarDic> carDics = carDicMapper.selectByCarNoIn(carNoList);
        //查询高风险车牌(大于80分)
        /*List<TietouCarDic> carDics = carDicMapper.listHighRiskCar(80);
        List<Integer> carNoIds = carDics.stream().map(TietouCarDic::getId).collect(Collectors.toList());
        XWPFDocument document = reportTemplateService.buildReport(carNoIds,"宜泸高速逃费",20190101,20190731);
        try (OutputStream os = new FileOutputStream("d://车辆报告.docx")) {
            document.write(os);
        } catch (IOException e) {
            log.error("{}", e);
            throw new MyException();
        }*/
        return Result.ok();
    }

    @GetMapping("downloadReport")
    @ApiOperation(value = "下载异常车辆报告")
    @PermissionCheck(ModuleConsts.RISK_CAR)
    @ParamToken
    public ResponseEntity<byte[]> downloadReport(@Validated({QueryValid.class}) CheatingListTimeSearchDto dto){
        int pageSize = 0;
        if (dto.getRiskFlag() == null || dto.getRiskFlag() != 0) {
            pageSize = 200;
        }
        if (dto.getLimit() != null){
            pageSize = dto.getLimit();
        }
        String beginDate = "2019-01-01";
        String endDate = "2019-08-31";
        dto.setBeginDate(beginDate);
        dto.setEndDate(endDate);
        dto.setPageNum(1);
        dto.setPageSize(30);
        PageVo<TietouFeatureStatisticGyh> pageVo = tietouScoreGyhService.listCheatingCarByTime(dto);
        List<Integer> idList = pageVo.getData().stream().map(TietouFeatureStatisticGyh::getVlpId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(idList)){
            throw new MyException("没有记录");
        }
        XWPFDocument document = reportTemplateService.buildReport(idList,"车辆报告",dto);
        if (document != null){
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()){
                document.write(bos);
                byte[] bytes = bos.toByteArray();
                return DownloadUtil.download(bytes,"车辆报告.docx");
            } catch (IOException e) {
                log.error("{}",e);
            }
        }
        return ResponseEntity.ok(TipsConsts.SERVER_ERROR.getBytes(Charset.forName("utf8")));
    }
}
