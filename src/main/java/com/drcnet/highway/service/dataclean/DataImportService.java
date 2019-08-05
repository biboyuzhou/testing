package com.drcnet.highway.service.dataclean;

import com.drcnet.highway.constants.CarSituationConsts;
import com.drcnet.highway.dao.TietouInboundMapper;
import com.drcnet.highway.dao.TietouMapper;
import com.drcnet.highway.entity.TietouInbound;
import com.drcnet.highway.entity.TietouOrigin;
import com.drcnet.highway.exception.MyException;
import com.drcnet.highway.service.TietouStationDicService;
import com.drcnet.highway.service.dic.TietouCarDicService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    @Resource
    private TietouInboundMapper tietouInboundMapper;


    @Transactional
    public void import2ndRoundData() {
        File rootDir = new File("C:\\SRIT\\项目\\高速\\数据\\收费站通行记录");
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
                        Integer ckId = tietouStationDicService.getOrInertByName(ck);
                        Integer rkId = tietouStationDicService.getOrInertByName(rk);
                        tietouOrigin.setCkId(ckId);
                        tietouOrigin.setRkId(rkId);
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

    /**
     * 导入入口数据
     */
    public void importInboundDataByExcel() {
        File file = new File("C:\\SRIT\\项目\\高速\\数据\\二绕\\二绕公司20辆入口记录");
        if(file.isDirectory()) {
            processDirectoty(file);
        } else if (file.isFile()) {
            processFile(file);
        }
    }

    private void processFile(File file) {
        if (file.isHidden()) {
            return;
        }
        String fileName = file.getName();
        if (!fileName.endsWith("xls") && !fileName.endsWith("xlsx")) {
            return;
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter monthFormat = DateTimeFormatter.ofPattern("yyyyMM");
        int count = 0;
        try(InputStream is = new FileInputStream(file)){
            Workbook workbook = null;
            if (fileName.endsWith("xls")) {
                workbook = new HSSFWorkbook(is);
            } else if (fileName.endsWith("xlsx")){
                workbook = new XSSFWorkbook(is);
            }
            if (workbook == null) {
                return;
            }
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            for (int i = 3; i < lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row.getCell(1) == null || StringUtils.isEmpty(row.getCell(1).getStringCellValue())) {
                    continue;
                }

                TietouInbound inbound = new TietouInbound();
                //设置进出站，进出车牌
                inbound.setRk(row.getCell(0).getStringCellValue());
                inbound.setEnvlp(row.getCell(1).getStringCellValue());
                //设置进出站时间
                String entimeDate = row.getCell(2).getStringCellValue();
                String entime = row.getCell(3).getStringCellValue();
                StringBuilder stringBuilder = new StringBuilder(entimeDate);
                stringBuilder.append(" ").append(entime);
                inbound.setEntime(LocalDateTime.parse(stringBuilder.toString(), dateTimeFormatter));

                String monthTime = inbound.getEntime().format(monthFormat);
                inbound.setMonthTime(Integer.parseInt(monthTime));
                //设置进站车型
                inbound.setEnvc(getCarTypeCode(row.getCell(4).getStringCellValue()));
                //设置进出站车情
                inbound.setEnvt(getCarSituationCode(row.getCell(5).getStringCellValue()));
                //车道
                inbound.setInlane(String.valueOf(row.getCell(6).getStringCellValue()));
                //卡号
                inbound.setCard(row.getCell(7).getStringCellValue());
                //流水号
                inbound.setInv(row.getCell(8).getStringCellValue());
                //设置进出站ID
                Integer rkId = tietouStationDicService.getOrInertByName(inbound.getRk());
                inbound.setRkId(rkId);
                //设置进出车牌ID
                Integer envlpId = tietouCarDicService.getOrInsertByName(inbound.getEnvlp());
                inbound.setEnvlpId(envlpId);
                inbound.setCreatime(LocalDateTime.now());
                tietouInboundMapper.insertSelective(inbound);
                count++;
            }
        } catch (IOException e) {
            log.error("{}",e);
            throw new MyException();
        }
        log.info("文件：{} 已导入完成，共导入 {} 条数据", fileName, count);
    }

    private void processDirectoty(File directory) {
        File[] listFiles = directory.listFiles();
        for (File file : listFiles) {
            //是文件夹
            if (file.isDirectory()) {
                processDirectoty(file);
            } else if(file.isFile()) {
                processFile(file);
            }
        }
    }
}
