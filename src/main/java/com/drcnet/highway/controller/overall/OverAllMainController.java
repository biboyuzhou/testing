package com.drcnet.highway.controller.overall;

import com.drcnet.highway.dto.RiskPeriodAmount;
import com.drcnet.highway.dto.response.StationRiskCountDto;
import com.drcnet.highway.dto.response.overall.EveryRoadRiskDataResponse;
import com.drcnet.highway.dto.response.overall.EveryRoadStationRiskResponse;
import com.drcnet.highway.dto.response.overall.MostRiskTypeResponse;
import com.drcnet.highway.service.overall.OverallService;
import com.drcnet.highway.util.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @Author jack
 * @Date: 2019/9/29 15:05
 * @Desc: 总数据首页controller
 **/
@Slf4j
@RestController
@RequestMapping("overall")
@Api(value = "OverAllMainController",tags = "总数首页接口")
public class OverAllMainController {

    @Resource
    private OverallService overallService;

    /**
     * 各个路段风险车辆数据查询接口
     * 新版总数据首页展示需要
     * @return
     */
    @ApiOperation(value = "各个路段风险车辆数据查询接口", notes = " /**\n" +
            "     * 二绕高风险数据\n" +
            "     */\n" +
            "    private Integer second;\n" +
            "    /**\n" +
            "     * 宜泸高风险数据\n" +
            "     */\n" +
            "    private Integer yl;\n" +
            "    /**\n" +
            "     * 巴广渝高风险数据\n" +
            "     */\n" +
            "    private Integer bgy;\n" +
            "    /**\n" +
            "     * 南大梁高风险数据\n" +
            "     */\n" +
            "    private Integer ndl;\n" +
            "    /**\n" +
            "     * 成自泸高风险数据\n" +
            "     */\n" +
            "    private Integer czl;\n" +
            "    /**\n" +
            "     * 成绵复线高风险数据\n" +
            "     */\n" +
            "    private Integer cmfx;\n" +
            "    /**\n" +
            "     * 绵南高风险数据\n" +
            "     */\n" +
            "    private Integer mn;\n" +
            "    /**\n" +
            "     * 内威荣高风险数据\n" +
            "     */\n" +
            "    private Integer nwr;\n" +
            "    /**\n" +
            "     * 叙古高风险数据\n" +
            "     */\n" +
            "    private Integer xg;\n" +
            "    /**\n" +
            "     * 宜叙高风险数据\n" +
            "     */\n" +
            "    private Integer yx;\n" +
            "    /**\n" +
            "     * 自隆高风险数据\n" +
            "     */\n" +
            "    private Integer zl;")
    @GetMapping("statisticEveryRoadRisk")
    public Result statisticEveryRoadRisk(){
        List<EveryRoadRiskDataResponse> response = overallService.statisticEveryRoadRisk();
        return Result.ok(response);
    }


    /**
     * 获取整个高速数据的风险车辆数据，包含高、中、低
     * 新版总数据首页展示需要
     * @return
     */
    @ApiOperation(value = "获取整个高速数据的风险车辆数据", notes = " /**\n" +
            "     * 无风险\n" +
            "     */\n" +
            "    private Integer non;\n" +
            "\n" +
            "    /**\n" +
            "     * 低风险\n" +
            "     */\n" +
            "    private Integer low;\n" +
            "\n" +
            "    /**\n" +
            "     * 中风险\n" +
            "     */\n" +
            "    private Integer middle;\n" +
            "\n" +
            "    /**\n" +
            "     * 高风险\n" +
            "     */\n" +
            "    private Integer high;")
    @GetMapping("statisticTotalRiskData")
    public Result statisticTotalRiskData(){
        RiskPeriodAmount riskPeriodAmount = overallService.statisticTotalRiskData();
        return Result.ok(riskPeriodAmount);
    }

    /**
     * 统计高风险站点top5
     * 新版总数据首页展示需要
     * @return
     */
    @ApiOperation(value = "统计高风险站点top5", notes = " private Integer ckId;\n" +
            "    private String ckName;\n" +
            "    /**\n" +
            "     * 经度\n" +
            "     */\n" +
            "    private String longitude;\n" +
            "    /**\n" +
            "     * 纬度\n" +
            "     */\n" +
            "    private String latitude;\n" +
            "    private Integer total;\n" +
            "    private Integer high;\n" +
            "    private Integer middle;\n" +
            "    private Integer low;")
    @GetMapping("statisticHighRiskStation")
    public Result statisticHighRiskStation(){
        List<StationRiskCountDto> riskCountDtoList = overallService.statisticHighRiskStation();
        return Result.ok(riskCountDtoList);
    }

    /**
     * 统计所有路段的站点风险数据
     * 新版总数据首页展示需要
     * @return
     */
    @ApiOperation(value = "统计所有路段的站点风险数据", notes = "/**\n" +
            "     * 二绕风险站点数据\n" +
            "     */\n" +
            "    private List<StationRiskCountDto> secondList;\n" +
            "\n" +
            "    /**\n" +
            "     * 宜泸风险站点数据\n" +
            "     */\n" +
            "    private List<StationRiskCountDto> ylList;\n" +
            "\n" +
            "    /**\n" +
            "     * 巴广渝风险站点数据\n" +
            "     */\n" +
            "    private List<StationRiskCountDto> bgyList;\n" +
            "\n" +
            "    /**\n" +
            "     * 南大梁风险站点数据\n" +
            "     */\n" +
            "    private List<StationRiskCountDto> ndlList;\n" +
            "\n" +
            "    /**\n" +
            "     * 成自泸风险站点数据\n" +
            "     */\n" +
            "    private List<StationRiskCountDto> czlList;\n" +
            "\n" +
            "    /**\n" +
            "     * 成绵复线风险站点数据\n" +
            "     */\n" +
            "    private List<StationRiskCountDto> cmfxList;\n" +
            "\n" +
            "    /**\n" +
            "     * 绵南风险站点数据\n" +
            "     */\n" +
            "    private List<StationRiskCountDto> mnList;\n" +
            "\n" +
            "    /**\n" +
            "     * 内威荣风险站点数据\n" +
            "     */\n" +
            "    private List<StationRiskCountDto> nwrList;\n" +
            "\n" +
            "    /**\n" +
            "     * 叙古风险站点数据\n" +
            "     */\n" +
            "    private List<StationRiskCountDto> xgList;\n" +
            "\n" +
            "    /**\n" +
            "     * 宜叙风险站点数据\n" +
            "     */\n" +
            "    private List<StationRiskCountDto> yxList;\n" +
            "\n" +
            "    /**\n" +
            "     * 自隆风险站点数据\n" +
            "     */\n" +
            "    private List<StationRiskCountDto> zlList;")
    @GetMapping("statisticEveryRoadRiskStation")
    public Result statisticEveryRoadRiskStation(){
        EveryRoadStationRiskResponse response = overallService.statisticEveryRoadRiskStation();
        return Result.ok(response);
    }

    /**
     * 统计风险类别分布top3
     * 新版总数据首页展示需要
     * @return
     */
    @ApiOperation(value = "统计风险类别分布top3", notes = "")
    @GetMapping("statisticTop3MostRiskType")
    public Result statisticTop3MostRiskType(){
        Map<Integer, MostRiskTypeResponse> map = overallService.statisticTop3MostRiskType();

        return Result.ok(map);
    }
}
