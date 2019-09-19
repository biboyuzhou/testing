package com.drcnet.highway.service.dataclean;

import com.drcnet.highway.config.ExcelImportUtil;
import com.drcnet.highway.constants.CarSituationConsts;
import com.drcnet.highway.dao.DataImportTaskMapper;
import com.drcnet.highway.dao.TietouInboundMapper;
import com.drcnet.highway.dao.TietouMapper;
import com.drcnet.highway.dao.TietouOutboundMapper;
import com.drcnet.highway.dto.CarNoDto;
import com.drcnet.highway.dto.TrafficStatisticsDto;
import com.drcnet.highway.entity.DataImportTask;
import com.drcnet.highway.entity.TietouInbound;
import com.drcnet.highway.entity.TietouOrigin;
import com.drcnet.highway.entity.TrafficStatistics;
import com.drcnet.highway.entity.usermodule.User;
import com.drcnet.highway.enums.BoundEnum;
import com.drcnet.highway.enums.FilePathEnum;
import com.drcnet.highway.enums.OriginalDataStateEnum;
import com.drcnet.highway.exception.MyException;
import com.drcnet.highway.service.TietouStationDicService;
import com.drcnet.highway.service.TrafficStatisticsService;
import com.drcnet.highway.service.dic.TietouCarDicService;
import com.drcnet.highway.util.DateUtils;
import com.drcnet.highway.util.EntityUtil;
import com.drcnet.highway.util.ExcelUtil;
import com.drcnet.highway.util.FilePathUtil;
import com.drcnet.usermodule.permission.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.aop.framework.AopContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.drcnet.highway.util.ExcelUtil.*;

/**
 * @Author: penghao
 * @CreateTime: 2019/7/23 10:38
 * @Description:
 */
@Service
@Slf4j
public class DataImportService {

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-M-d H:mm:ss");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm:ss");
    private DateTimeFormatter monthFormat = DateTimeFormatter.ofPattern("yyyyMM");

    @Resource
    private TietouStationDicService tietouStationDicService;
    @Resource
    private TietouCarDicService tietouCarDicService;
    @Resource
    private TietouMapper tietouMapper;
    @Resource
    private DataImportTaskMapper dataImportTaskMapper;
    @Resource
    private TietouOutboundMapper tietouOutboundMapper;
    @Resource
    private TietouInboundMapper tietouInboundMapper;
    @Resource
    private TrafficStatisticsService trafficStatisticsService;

    private static final String DATE_TIME_PATTERN = "yyyy-M-d H:mm:ss";

    @Transactional
    public void importNewData() {
        File rootDir = new File("C:\\Users\\18419\\Desktop\\常用\\高速\\0910");
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
                    Workbook workbook = new XSSFWorkbook(is);
                    Sheet sheet = workbook.getSheetAt(1);
                    int lastRowNum = sheet.getLastRowNum();
                    for (int i = 3; i < lastRowNum; i++) {
                        Row row = sheet.getRow(i);
                        TietouOrigin tietouOrigin = new TietouOrigin();
                        //设置进出站，进出车牌
                        tietouOrigin.setRk(row.getCell(0).getStringCellValue());
                        tietouOrigin.setCk(row.getCell(4).getStringCellValue());
                        tietouOrigin.setEnvlp(row.getCell(1).getStringCellValue());
                        tietouOrigin.setVlp(row.getCell(5).getStringCellValue());
                        //设置进出站时间
                        String entimeDate = row.getCell(2).getStringCellValue();
                        String extimeDate = row.getCell(6).getStringCellValue();
                        tietouOrigin.setEntime(LocalDateTime.parse(entimeDate,dateTimeFormatter));
                        tietouOrigin.setExtime(LocalDateTime.parse(extimeDate,dateTimeFormatter));
                        String monthTime = tietouOrigin.getExtime().format(monthFormat);
                        tietouOrigin.setMonthTime(Integer.parseInt(monthTime));

                        tietouOrigin.setVc(1);
                        tietouOrigin.setEnvc(1);
                        //设置进出站车情
                        tietouOrigin.setEnvt(1);
                        tietouOrigin.setVt(1);
                        //车道
                        int exlane =Integer.parseInt(row.getCell(9).getStringCellValue());
                        tietouOrigin.setExlane(String.valueOf(exlane));
                        //通行费用和减免费用
                        double lastMoney = row.getCell(11).getNumericCellValue();
                        double freeMoney = row.getCell(12).getNumericCellValue();
                        tietouOrigin.setLastmoney(BigDecimal.valueOf(lastMoney));
                        tietouOrigin.setFreemoney(BigDecimal.valueOf(freeMoney));
                        //总重
                        String weight = String.valueOf(row.getCell(13).getNumericCellValue());
                        tietouOrigin.setTotalweight(Integer.parseInt(weight.substring(0, weight.indexOf("."))));
                        //轴数
                        tietouOrigin.setAxlenum(2);
                        String weightLimit = String.valueOf(row.getCell(15).getNumericCellValue());
                        tietouOrigin.setWeightLimitation(Integer.parseInt(weightLimit.substring(0, weightLimit.indexOf("."))));
                        //总里程
                        String distance = String.valueOf(row.getCell(16).getNumericCellValue());
                        tietouOrigin.setTolldistance(Integer.parseInt(distance.substring(0, distance.indexOf("."))));
                        //卡号
                        tietouOrigin.setCard(row.getCell(17).getStringCellValue());
                        tietouOrigin.setFlagstationinfo(row.getCell(20).getStringCellValue());
                        tietouOrigin.setRealflagstationinfo(row.getCell(19).getStringCellValue());
                        tietouOrigin.setInv(row.getCell(18).getStringCellValue());
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
                        CarNoDto envlpId = tietouCarDicService.getOrInsertByName(envlp, 1);
                        CarNoDto vlpId = tietouCarDicService.getOrInsertByName(vlp, 1);
                        tietouOrigin.setEnvlpId(envlpId.getId());
                        tietouOrigin.setVlpId(vlpId.getId());
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

    @Transactional
    public void import2ndRoundData() {
        File rootDir = new File("C:\\SRIT\\项目\\高速\\数据\\收费站通行记录");
        File[] parentDirs = rootDir.listFiles();
        for (File parentDir : parentDirs) {
            File[] files = parentDir.listFiles();
            if (files == null) {
                continue;
            }
            for (File file : files) {
                try (InputStream is = new FileInputStream(file)) {
                    Workbook workbook = new HSSFWorkbook(is);
//                    importOutboundAction(workbook, dataDoc);
                } catch (IOException e) {
                    log.error("{}", e);
                    throw new MyException();
                }
                log.info("文件:{}，已导入成功", file.getName());
            }
        }
    }

    @Async("taskExecutor")
    public void importAction(Workbook workbook, DataImportTask dataDoc, BoundEnum bound, DataImportService dataImportService) {
        //设置状态为导入中
        dataDoc.setState(OriginalDataStateEnum.IMPORTING.code);
        dataImportTaskMapper.updateByPrimaryKeySelective(dataDoc);
        try {
            if (bound == BoundEnum.OUTBOUND) {
                dataImportService.importOutboundAction(workbook, dataDoc, bound);
            } else if (bound == BoundEnum.INBOUND) {
                dataImportService.importInboundAction(workbook, dataDoc, bound);
            }
        } catch (Exception e) {
            log.error("数据导入失败,日志ID:{}", dataDoc.getId());
            log.error("{}", e);
            saveErrorMsg("程序异常，请查看日志", dataDoc.getId());
        }
    }

    @Transactional
    public void importInboundAction(Workbook workbook, DataImportTask dataDoc, BoundEnum bound) {
        long timeMillis = System.currentTimeMillis();
        Sheet sheet = workbook.getSheetAt(0);
        int lastRowNum = sheet.getLastRowNum();
        int firstDataRow = ExcelImportUtil.getFirstDataRow(sheet, bound);
        int success = 0;
        int repeat = 0;
        int useless = 0;
        List<TietouInbound> usefulData = new ArrayList<>();
        SimpleDateFormat timeFormat = new SimpleDateFormat("H:mm:ss");
        for (int i = firstDataRow; i <= lastRowNum; i++) {
            Row row = sheet.getRow(i);
            if (ExcelUtil.isAllBlank(row.getCell(1))) {
                continue;
            }
            TietouInbound inbound = new TietouInbound();
            //设置进出站，进出车牌
            inbound.setRk(getCellString(row.getCell(0)));
            //设置进出站时间
            String entimeDate = getCellDateTimeStr(row.getCell(2), dateFormatter);
            String entime = getCellDateTimeStr(row.getCell(3), timeFormat);
            inbound.setEntime(DateUtils.parseTime(StringUtils.trimWhitespace(entimeDate) + " " + StringUtils.trimWhitespace(entime), dateTimeFormatter));
            LocalDateTime entimeFinal = inbound.getEntime();
            if (entimeFinal != null) {
                inbound.setMonthTime(Integer.parseInt(entimeFinal.format(monthFormat)));
            }
            //设置进站车型
            int envc = getCarTypeCode(getCellString(row.getCell(4)));
            inbound.setEnvc(envc);
            //设置进出站车情
            inbound.setEnvt(getCarSituationCode(getCellString(row.getCell(5))));
            //车道
            inbound.setInlane(String.valueOf(getCellString(row.getCell(6))));
            //卡号
            inbound.setCard(getCellString(row.getCell(7)));
            //流水号
            inbound.setInv(getCellString(row.getCell(8)));
            //设置进出站ID
            Integer rkId = tietouStationDicService.getOrInertByName(inbound.getRk());
            inbound.setRkId(rkId);
            //设置进出车牌ID
            String envlp = getCellString(row.getCell(1));
            CarNoDto carNoDto = tietouCarDicService.getOrInsertByName(envlp, envc);
            inbound.setEnvlpId(carNoDto.getId());
            inbound.setEnvlp(carNoDto.getCarNo());
            inbound.setCreatime(LocalDateTime.now());
            if (inbound.isUseful()) {
                int affectRow = tietouInboundMapper.insertIgnore(inbound);
                if (affectRow > 0) {
                    success++;
                    usefulData.add(inbound);
                } else {
                    repeat++;
                }
            } else {
                useless++;
            }
        }
        //更新日志
        saveImportedState(dataDoc, success, repeat, useless);
        log.info("入口数据导入成功,文件:{},耗时:{}秒,成功数:{},重复数据:{},无效数据:{}", dataDoc.getPath(), (System.currentTimeMillis() - timeMillis) / 1000,
                success, repeat, useless);
        if (!usefulData.isEmpty()) {
            Map<TrafficStatisticsDto, Long> statisticsMap = usefulData.stream().collect(Collectors.groupingBy(TrafficStatisticsDto::new, Collectors.counting()));
            trafficStatisticsAction(statisticsMap, bound, dataDoc.getId());
        }
    }

    /**
     * 调用增量数据统计
     */
    private void trafficStatisticsAction(Map<TrafficStatisticsDto, Long> statisticsMap, BoundEnum bound, Integer taskId) {
        List<TrafficStatistics> statisticsList = extractionTrafficStatistics(statisticsMap, bound, true);
        trafficStatisticsService.incrementDataStatistics(statisticsList, taskId, trafficStatisticsService);
    }

    private void trafficStatisticsAction(Map<TrafficStatisticsDto, Long> statisticsMap, Map<TrafficStatisticsDto, Long> inboundMap, BoundEnum bound, Integer taskId) {
        List<TrafficStatistics> statisticsList = statisticsMap.entrySet().stream().map(var -> {
            TrafficStatisticsDto key = var.getKey();
            TrafficStatistics trafficStatistics = EntityUtil.copyNotNullFields(key, new TrafficStatistics());
            trafficStatistics.setAmount(var.getValue().intValue());
            trafficStatistics.setBoundType(bound.code);
            Long inAmount = inboundMap.get(key);
            if (inAmount != null) {
                trafficStatistics.setInAmount(inAmount.intValue());
                inboundMap.remove(key);
            } else {
                trafficStatistics.setInAmount(0);
            }
            return trafficStatistics;
        }).collect(Collectors.toList());
        List<TrafficStatistics> inboundStatistics = extractionTrafficStatistics(inboundMap, bound, false);
        statisticsList.addAll(inboundStatistics);
        trafficStatisticsService.incrementDataStatistics(statisticsList, taskId, trafficStatisticsService);
    }

    /**
     * @param amountFlag true:设置amount,false:设置inAmount
     */
    private List<TrafficStatistics> extractionTrafficStatistics(Map<TrafficStatisticsDto, Long> statisticsMap, BoundEnum bound, boolean amountFlag) {
        return statisticsMap.entrySet().stream().map(var -> {
            TrafficStatisticsDto key = var.getKey();
            TrafficStatistics trafficStatistics = EntityUtil.copyNotNullFields(key, new TrafficStatistics());
            if (amountFlag) {
                trafficStatistics.setAmount(var.getValue().intValue());
                trafficStatistics.setInAmount(0);
            } else {
                trafficStatistics.setInAmount(var.getValue().intValue());
                trafficStatistics.setAmount(0);
            }
            trafficStatistics.setBoundType(bound.code);
            return trafficStatistics;
        }).collect(Collectors.toList());
    }


    @Transactional
    public void importOutboundAction(Workbook workbook, DataImportTask dataDoc, BoundEnum bound) {
        long timeMillis = System.currentTimeMillis();
        log.info("开始导入文件:{} 的数据", dataDoc.getPath());
        int success = 0;
        int repeat = 0;
        int useless = 0;
        Sheet sheet = workbook.getSheetAt(0);
        int lastRowNum = sheet.getLastRowNum();
        int firstDataRow = ExcelImportUtil.getFirstDataRow(sheet, bound);
        List<TietouOrigin> usefulData = new ArrayList<>(64);


        for (int i = firstDataRow; i <= lastRowNum; i++) {
            Row row = sheet.getRow(i);
            if (ExcelUtil.isAllBlank(row.getCell(3), row.getCell(7), row.getCell(19))) {
                continue;
            }
            TietouOrigin tietouOrigin = new TietouOrigin();
            //设置进出站，进出车牌
            tietouOrigin.setRk(getCellString(row.getCell(10)));
            tietouOrigin.setCk(getCellString(row.getCell(19)));

            //设置进出站时间
            tietouOrigin.setEntime(getCellDateTime(row.getCell(12), dateTimeFormatter));
            tietouOrigin.setExtime(getCellDateTime(row.getCell(7), dateTimeFormatter));
            LocalDateTime extime = tietouOrigin.getExtime();
            if (extime != null) {
                tietouOrigin.setMonthTime(Integer.parseInt(extime.format(monthFormat)));
            }
            //设置进出站车型
            String vcStr = getCellString(row.getCell(9));
            int vc = getCarTypeCode(vcStr);
            int envc = Integer.parseInt(getCellString(row.getCell(14)));
            tietouOrigin.setVc(vc);
            tietouOrigin.setEnvc(envc);
            //设置进出站车情
            tietouOrigin.setEnvt(Integer.parseInt(getCellString(row.getCell(15))));
            String vtStr = getCellString(row.getCell(8));
            tietouOrigin.setVt(getCarSituationCode(vtStr));
            //车道
            int exlane = Integer.parseInt(getCellString(row.getCell(4)));
            tietouOrigin.setExlane(String.valueOf(exlane));
            //通行费用和减免费用
            double lastMoney = Double.parseDouble(getCellString(row.getCell(35)));
            double freeMoney = Double.parseDouble(getCellString(row.getCell(54)));
            tietouOrigin.setLastmoney(BigDecimal.valueOf(lastMoney));
            tietouOrigin.setFreemoney(BigDecimal.valueOf(freeMoney));
            //总重
            tietouOrigin.setTotalweight(Integer.parseInt(getCellString(row.getCell(30))));
            //总重限制
            tietouOrigin.setWeightLimitation(Integer.parseInt(getCellString(row.getCell(31))));
            //轴数
            tietouOrigin.setAxlenum(Integer.parseInt(getCellString(row.getCell(33))));
            //总里程
            tietouOrigin.setTolldistance(Integer.parseInt(getCellString(row.getCell(28))));
            //卡号
            tietouOrigin.setCard(getCellString(row.getCell(1)));
            //设置进出站ID
            String ck = tietouOrigin.getCk();
            String rk = tietouOrigin.getRk();
            Integer ckId = tietouStationDicService.getOrInertByName(ck);
            Integer rkId = tietouStationDicService.getOrInertByName(rk);
            tietouOrigin.setCkId(ckId);
            tietouOrigin.setRkId(rkId);
            //设置进出车牌ID
            String envlp = getCellString(row.getCell(13));
            String vlp = getCellString(row.getCell(3));
            CarNoDto envlpDto = tietouCarDicService.getOrInsertByCarNo(envlp, envc);
            CarNoDto vlpDto = tietouCarDicService.getOrInsertByCarNo(vlp, vc);
            tietouOrigin.setEnvlpId(envlpDto.getId());
            tietouOrigin.setEnvlp(envlpDto.getCarNo());
            tietouOrigin.setVlpId(vlpDto.getId());
            tietouOrigin.setVlp(vlpDto.getCarNo());
            //判断数据是否有效
            if (tietouOrigin.isUseful()) {
                //先新增到tietou表
                tietouOrigin.setTietouFlag(true);
                int affectRow = tietouMapper.insertIgnore(tietouOrigin);
                if (affectRow > 0) {
                    success++;
                    //新增到tietou_2019表
                    tietouOrigin.setTietouFlag(false);
                    tietouMapper.insertIgnore(tietouOrigin);
                    usefulData.add(tietouOrigin);
                } else {
                    repeat++;
                }
            } else {
                useless++;
            }
        }
        //更新日志
        saveImportedState(dataDoc, success, repeat, useless);
        log.info("出口数据导入成功,文件:{},耗时:{}秒,成功数:{},重复数据:{},无效数据:{}", dataDoc.getPath(), (System.currentTimeMillis() - timeMillis) / 1000,
                success, repeat, useless);
        //进行增量数据统计
        if (!usefulData.isEmpty()) {
            Map<TrafficStatisticsDto, Long> outBoundMap = usefulData.stream().collect(Collectors.groupingBy(var -> new TrafficStatisticsDto(var, BoundEnum.OUTBOUND), Collectors.counting()));
            Map<TrafficStatisticsDto, Long> inBoundMap = usefulData.stream().collect(Collectors.groupingBy(var -> new TrafficStatisticsDto(var, BoundEnum.INBOUND), Collectors.counting()));
            trafficStatisticsAction(outBoundMap, inBoundMap, bound, dataDoc.getId());
        }

    }


    /**
     * 将汉字类型的车型信息转换为数字，例如货1，应输出 11
     */
    private int getCarTypeCode(String carType) {
        return CarSituationConsts.CAR_TYPE_MAP.getOrDefault(carType, 0);
    }

    /**
     * 将汉字类型的车车情信息转换为数字，例如普通车，输出1
     */
    private int getCarSituationCode(String situation) {
        return CarSituationConsts.SITUATION_MAP.getOrDefault(situation, 0);
    }

    /**
     * 导入入口数据
     */
    public void importInboundDataByExcel() {
        File file = new File("C:\\SRIT\\项目\\高速\\数据\\二绕\\二绕公司20辆入口记录");
        if (file.isDirectory()) {
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
        try (InputStream is = new FileInputStream(file)) {
            Workbook workbook = null;
            if (fileName.endsWith("xls")) {
                workbook = new HSSFWorkbook(is);
            } else if (fileName.endsWith("xlsx")) {
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
                int envc = getCarTypeCode(row.getCell(4).getStringCellValue());
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
                CarNoDto carNoDto = tietouCarDicService.getOrInsertByName(inbound.getEnvlp(), envc);
                inbound.setEnvlpId(carNoDto.getId());
                inbound.setEnvlp(carNoDto.getCarNo());
                inbound.setCreatime(LocalDateTime.now());
                tietouInboundMapper.insertSelective(inbound);
                count++;
            }
        } catch (IOException e) {
            log.error("{}", e);
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
            } else if (file.isFile()) {
                processFile(file);
            }
        }
    }

    /**
     * 文件上传
     *
     * @param multipartFile
     * @param type
     */
    @Transactional
    public void uploadFile(MultipartFile multipartFile, Integer type) throws IOException {
        String filename = multipartFile.getOriginalFilename();
        String md5 = DigestUtils.md5DigestAsHex(multipartFile.getInputStream());
        Integer userId = ((User) UserContext.get()).getId();
        DataImportTask query = new DataImportTask();
        query.setMd5(md5);
        query.setBoundType(type);
        query.setUserId(userId);
        query.setFilename(filename);
        query.setStatisticFlag(false);
        DataImportTask dataDoc = dataImportTaskMapper.selectByMd5AndType(md5, type);
        if (dataDoc != null && dataDoc.getState() != OriginalDataStateEnum.IMPORTED_DEFEAT.code) {
            throw new MyException("该文件已存在，请勿重复上传");
        }
        String path = LocalDate.now() + "/" + filename;
        String fullPath = FilePathUtil.getFilePath(path, FilePathEnum.getBound(type));
        File file = new File(fullPath);
        FilePathUtil.createPath(file.getParent());

        multipartFile.transferTo(file);
        query.setPath(path);
        query.setCreateTime(LocalDateTime.now());
        query.setState(OriginalDataStateEnum.UPLOADED.code);
        //插入记录
        dataImportTaskMapper.insertSelective(query);
        BoundEnum bound = BoundEnum.getBound(type);
        //导入数据
        importOutboundDataByExcel(fullPath, query, bound);
    }

    /**
     * 导入出口数据至数据库
     */

    private void importOutboundDataByExcel(String fullPath, DataImportTask dataDoc, BoundEnum bound) {
        Workbook workbook;
        String errmsg;
        File file = new File(fullPath);
        try (InputStream is = new FileInputStream(file)) {
            workbook = getWorkbook(is, file.getName());
        } catch (IOException e) {
            log.error("{},{}", e, dataDoc);
            errmsg = "文件读取失败";
            throw new MyException(errmsg);
        }
        //检查文件是否符合格式要求
        if (!ExcelImportUtil.isTemplateExcel(workbook, bound)) {
            errmsg = "excel格式不是标准格式，请以模板excel格式上传";
            throw new MyException(errmsg);
        }
        //异步开始导入数据
        DataImportService dataImportService = (DataImportService) AopContext.currentProxy();
        dataImportService.importAction(workbook, dataDoc, bound, dataImportService);
    }

    /**
     * 保存上传日志错误信息
     */
    private void saveErrorMsg(String errmsg, Integer id) {
        DataImportTask importTask = new DataImportTask();
        importTask.setId(id);
        importTask.setState(OriginalDataStateEnum.IMPORTED_DEFEAT.code);
        importTask.setErrorMsg(errmsg);
        dataImportTaskMapper.updateByPrimaryKeySelective(importTask);
    }

    private void saveImportedState(DataImportTask dataDoc, int success, int repeat, int useless) {
        dataDoc.setSuccessAmount(success);
        dataDoc.setRepeatAmount(repeat);
        dataDoc.setFailureAmount(useless);
        dataDoc.setFinishTime(LocalDateTime.now());
        dataDoc.setState(OriginalDataStateEnum.IMPORTED.code);
        dataImportTaskMapper.updateByPrimaryKeySelective(dataDoc);
    }

    public void importTxtOriginalData(String separator, File file) {
        AtomicLong atomicLong = new AtomicLong();

        DataImportService dataImportService = (DataImportService) AopContext.currentProxy();
        try (Reader reader = new FileReader(file);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                dataImportService.insertNewOriginalRecord(line, separator, atomicLong);
            }
        } catch (IOException e) {
            log.error("{}", e);
            throw new MyException();
        }
    }

    @Async("taskExecutor")
    public void insertNewOriginalRecord(String line, String separator, AtomicLong atomicLong) {
        String[] split = line.split(separator);
        TietouOrigin tietouOrigin = new TietouOrigin();
        tietouOrigin.setEntime(LocalDateTime.parse(split[0].trim(), dateTimeFormatter));
        tietouOrigin.setExtime(LocalDateTime.parse(split[5].trim(), dateTimeFormatter));
        tietouOrigin.setRk(split[1].trim());
        tietouOrigin.setCk(split[6].trim());
        tietouOrigin.setEnvlp(split[2].trim());
        tietouOrigin.setVlp(split[7].trim());
        tietouOrigin.setEnvc(Integer.parseInt(split[4]));
        tietouOrigin.setVc(Integer.parseInt(split[8]));
        tietouOrigin.setEnvt(Integer.parseInt(split[3]));
        tietouOrigin.setVt(Integer.parseInt(split[9]));
        tietouOrigin.setAxlenum(Integer.parseInt(split[15]));
        tietouOrigin.setExlane(split[10]);
        tietouOrigin.setOper(split[11]);
        tietouOrigin.setTotalweight(Integer.parseInt(split[14]));
        tietouOrigin.setTolldistance(Integer.parseInt(split[16]));
        if (split.length == 21) {
            tietouOrigin.setCard(split[17]);
            tietouOrigin.setFlagstationinfo(split[18]);
            tietouOrigin.setRealflagstationinfo(split[19]);
            tietouOrigin.setInv(split[20]);
        } else if (split.length == 20) {
            tietouOrigin.setCard(split[17]);
            tietouOrigin.setFlagstationinfo(split[18]);
            tietouOrigin.setRealflagstationinfo(split[19]);
        } else if (split.length == 19) {
            tietouOrigin.setCard(split[17]);
            tietouOrigin.setFlagstationinfo(split[18]);
        } else if (split.length == 18) {
            tietouOrigin.setCard(split[17]);
        }

        String lastmoney = split[12].startsWith(".") ? 0 + split[12] : split[12];
        String freemoney = split[13].startsWith(".") ? 0 + split[13] : split[13];
        tietouOrigin.setLastmoney(BigDecimal.valueOf(Double.parseDouble(lastmoney)));
        tietouOrigin.setFreemoney(BigDecimal.valueOf(Double.parseDouble(freemoney)));
        tietouMapper.insertIgnore(tietouOrigin);
        long success = atomicLong.incrementAndGet();
        if (success % 200000 == 0) {
            log.info("已新增：{} 条数据", success);
        }
    }


}
