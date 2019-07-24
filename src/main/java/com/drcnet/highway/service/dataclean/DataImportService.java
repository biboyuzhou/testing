package com.drcnet.highway.service.dataclean;

import com.drcnet.highway.constants.CarSituationConsts;
import com.drcnet.highway.dao.TietouMapper;
import com.drcnet.highway.entity.TietouOrigin;
import com.drcnet.highway.entity.dic.StationDic;
import com.drcnet.highway.exception.MyException;
import com.drcnet.highway.service.TietouStationDicService;
import com.drcnet.highway.service.dic.TietouCarDicService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Author: penghao
 * @CreateTime: 2019/7/23 10:38
 * @Description:
 */
@Service
@Slf4j
public class DataImportService {

    private static final String DATE_TIME_PATTERN = "yyyy-M-d H:mm:ss";

    @Resource
    private TietouStationDicService tietouStationDicService;
    @Resource
    private TietouCarDicService tietouCarDicService;
    @Resource
    private TietouMapper tietouMapper;


    @Transactional
    public void import2ndRoundData() {
        File rootDir = new File("D:\\doc\\高速公路车辆分析\\北段各站出口通行数据201.7.1-7.18");
        File[] parentDirs = rootDir.listFiles();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
        DateTimeFormatter monthFormat = DateTimeFormatter.ofPattern("yyyyMM");
        for (File parentDir : parentDirs) {
            File[] files = parentDir.listFiles();
            if (files == null){
                continue;
            }
            for (File file : files) {
                try(InputStream is = new FileInputStream(file)){
                    Workbook workbook = new HSSFWorkbook(is);
                    Sheet sheet = workbook.getSheetAt(0);
                    int lastRowNum = sheet.getLastRowNum();
                    for (int i = 4; i < lastRowNum; i++) {
                        Row row = sheet.getRow(i);
                        TietouOrigin tietouOrigin = new TietouOrigin();
                        //设置进出站，进出车牌
                        tietouOrigin.setRk(row.getCell(10).getStringCellValue());
                        tietouOrigin.setCk(row.getCell(19).getStringCellValue());
                        tietouOrigin.setEnvlp(row.getCell(13).getStringCellValue());
                        tietouOrigin.setVlp(row.getCell(3).getStringCellValue());
                        //设置进出站时间
                        String entimeDate = row.getCell(12).getStringCellValue();
                        String extimeDate = row.getCell(7).getStringCellValue();
                        tietouOrigin.setEntime(LocalDateTime.parse(entimeDate,dateTimeFormatter));
                        tietouOrigin.setExtime(LocalDateTime.parse(extimeDate,dateTimeFormatter));
                        String monthTime = tietouOrigin.getExtime().format(monthFormat);
                        tietouOrigin.setMonthTime(Integer.parseInt(monthTime));
                        //设置进出站车型
                        String vcStr = row.getCell(9).getStringCellValue();
                        tietouOrigin.setVc(getCarTypeCode(vcStr));
                        tietouOrigin.setEnvc(Integer.parseInt(row.getCell(14).getStringCellValue()));
                        //设置进出站车情
                        tietouOrigin.setEnvt(Integer.parseInt(row.getCell(15).getStringCellValue()));
                        String vtStr = row.getCell(9).getStringCellValue();
                        tietouOrigin.setVt(getCarSituationCode(vtStr));
                        //车道
                        int exlane =Integer.parseInt(row.getCell(4).getStringCellValue());
                        tietouOrigin.setExlane(String.valueOf(exlane));
                        //通行费用和减免费用
                        double lastMoney = Double.parseDouble(row.getCell(35).getStringCellValue());
                        double freeMoney = Double.parseDouble(row.getCell(54).getStringCellValue());
                        tietouOrigin.setLastmoney(BigDecimal.valueOf(lastMoney));
                        tietouOrigin.setFreemoney(BigDecimal.valueOf(freeMoney));
                        //总重
                        tietouOrigin.setTotalweight(Integer.parseInt(row.getCell(30).getStringCellValue()));
                        //轴数
                        tietouOrigin.setAxlenum(Integer.parseInt(row.getCell(33).getStringCellValue()));
                        //总里程
                        tietouOrigin.setTolldistance(Integer.parseInt(row.getCell(28).getStringCellValue()));
                        //卡号
                        tietouOrigin.setCard(row.getCell(1).getStringCellValue());
                        //设置进出站ID
                        String ck = tietouOrigin.getCk();
                        String rk = tietouOrigin.getRk();
                        StationDic ckDic = tietouStationDicService.getOrInertByName(ck);
                        StationDic rkDic = tietouStationDicService.getOrInertByName(rk);
                        tietouOrigin.setCkId(ckDic.getId());
                        tietouOrigin.setRkId(rkDic.getId());
                        //设置进出车牌ID
                        String envlp = tietouOrigin.getEnvlp();
                        String vlp = tietouOrigin.getVlp();
                        Integer envlpId = tietouCarDicService.getOrInsertByName(envlp);
                        Integer vlpId = tietouCarDicService.getOrInsertByName(vlp);
                        tietouOrigin.setEnvlpId(envlpId);
                        tietouOrigin.setVlpId(vlpId);
                        tietouMapper.insertSelective(tietouOrigin);
                    }
                } catch (IOException e) {
                    log.error("{}",e);
                    throw new MyException();
                }
                log.info("文件:{}，已导入成功",file.getName());
            }
        }
    }

    /**
     * 将汉字类型的车型信息转换为数字，例如货1，应输出 11
     */
    private int getCarTypeCode(String carType){
        return CarSituationConsts.CAR_TYPE_MAP.getOrDefault(carType,0);
    }
    /**
     * 将汉字类型的车车情信息转换为数字，例如普通车，输出1
     */
    private int getCarSituationCode(String situation){
        return CarSituationConsts.SITUATION_MAP.getOrDefault(situation,0);
    }

}
