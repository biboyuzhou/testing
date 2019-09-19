package com.drcnet.highway.service.dic;

import com.drcnet.highway.constants.ConfigConsts;
import com.drcnet.highway.constants.TipsConsts;
import com.drcnet.highway.dao.TietouCarDicMapper;
import com.drcnet.highway.dao.TietouFeatureStatisticMapper;
import com.drcnet.highway.dto.CarNoDto;
import com.drcnet.highway.dto.SuccessAmountDto;
import com.drcnet.highway.dto.request.BlackListInsertDto;
import com.drcnet.highway.entity.dic.TietouCarDic;
import com.drcnet.highway.exception.MyException;
import com.drcnet.highway.service.TietouBlackListService;
import com.drcnet.highway.util.domain.CarNoUtil;
import com.drcnet.highway.util.templates.BaseService;
import com.drcnet.highway.util.templates.MyMapper;
import com.drcnet.highway.vo.PageVo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/8 14:31
 * @Description:
 */
@Slf4j
@Service
public class TietouCarDicService implements BaseService<TietouCarDic, Integer> {

    @Resource
    private TietouCarDicMapper thisMapper;
    @Resource
    private TietouBlackListService tietouBlackListService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private TietouFeatureStatisticMapper tietouFeatureStatisticMapper;

    /**
     * 模糊查询车牌号
     *
     * @param carNo   车牌号
     * @param carType
     * @return
     */
    public List<TietouCarDic> queryCarNo(String carNo, Integer carType) {
        return thisMapper.queryCarNo(carNo + "%", carType);
    }

    @Override
    public MyMapper<TietouCarDic> getMapper() {
        return thisMapper;
    }

    /**
     * 将车牌加入至白名单
     *
     * @param carNo 车牌号
     * @param flag
     */
    public void addWhiteList(String carNo, Integer flag) {
        TietouCarDic query = new TietouCarDic();
        query.setCarNo(carNo);
        TietouCarDic tietouCarDic = thisMapper.selectByCarNo(carNo);
        if (tietouCarDic == null) {
            if (flag == 0) {
                throw new MyException("该车牌没有被加入白名单!");
            }
            query.setCreateTime(LocalDateTime.now());
            query.setUseFlag(false);
            query.setWhiteFlag(true);
            thisMapper.insertNewCar(query);
        } else {
            if (flag == 1 && !tietouCarDic.getWhiteFlag()) {
                tietouCarDic.setWhiteFlag(true);
                tietouCarDic.setUseFlag(false);
                thisMapper.updateWhiteAndUseFlag(tietouCarDic);
                //如果加入了白名单，则取消黑名单
                tietouBlackListService.cancelBlack(tietouCarDic.getId());
                //加入白名单则将异常打分置为0
                tietouFeatureStatisticMapper.updateScoreByVlpId(tietouCarDic.getId());
            } else if (flag == 0 && tietouCarDic.getWhiteFlag()) {
                tietouCarDic.setWhiteFlag(false);
                tietouCarDic.setUseFlag(true);
                thisMapper.updateWhiteAndUseFlag(tietouCarDic);

                //删除白名单则将异常打分分数恢复
                tietouFeatureStatisticMapper.updateScoreFromGyhByVlpId(tietouCarDic.getId());
            } else {
                throw new MyException("添加失败，已有该数据");
            }
        }


    }

    /**
     * 查询白名单列表
     */
    public PageVo<TietouCarDic> listWhite(Integer pageNum, Integer pageSize) {
        TietouCarDic query = new TietouCarDic();
        query.setWhiteFlag(true);
        PageHelper.startPage(pageNum, pageSize);
        List<TietouCarDic> res = thisMapper.selectWhiteFlag();
        return PageVo.of(res);
    }

    /**
     * 将文件上传上来的excel添加至白名单
     *
     * @param file 文件
     */
    public SuccessAmountDto uploadWhiteList(MultipartFile file) {
        SuccessAmountDto successAmountDto = new SuccessAmountDto();
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new MyException("文件异常请重试");
        }
        if (!originalFilename.endsWith("xls") && !originalFilename.endsWith("xlsx")) {
            throw new MyException("文件类型错误，请上传Excel文件");
        }
        int successAmount = 0;
        int total = 0;
        try (InputStream is = file.getInputStream()) {
            XSSFWorkbook xssfSheets = new XSSFWorkbook(is);
            XSSFSheet sheet = xssfSheets.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            for (int i = 1; i <= lastRowNum; i++) {
                XSSFRow row = sheet.getRow(i);
                XSSFCell cell = row.getCell(0);
                if (cell == null) {
                    continue;
                }

                String carNo;
                if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING) {
                    carNo = cell.getStringCellValue();
                } else if (cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
                    carNo = String.valueOf(cell.getNumericCellValue());
                } else {
                    continue;
                }
                try {
                    addWhiteList(carNo, 1);
                    ++successAmount;
                } catch (MyException e) {
                    log.error("加入白名单异常:{}", e.getMessage());
                }
                ++total;
            }
            successAmountDto.setSuccess(successAmount);
            successAmountDto.setTotal(total);
        } catch (IOException e) {
            log.error("{}", e);
            throw new MyException(TipsConsts.SERVER_ERROR);
        }
        return successAmountDto;
    }

    /**
     * 上传文件至黑名单
     *
     * @param file
     * @return
     */
    public SuccessAmountDto uploadBlackList(MultipartFile file) {
        SuccessAmountDto successAmountDto = new SuccessAmountDto();
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new MyException("文件异常请重试");
        }
        if (!originalFilename.endsWith("xls") && !originalFilename.endsWith("xlsx")) {
            throw new MyException("文件类型错误，请上传Excel文件");
        }
        int successAmount = 0;
        int total = 0;
        try (InputStream is = file.getInputStream()) {
            XSSFWorkbook xssfSheets = new XSSFWorkbook(is);
            XSSFSheet sheet = xssfSheets.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            for (int i = 1; i <= lastRowNum; i++) {

                XSSFRow row = sheet.getRow(i);
                XSSFCell carNoCell = row.getCell(0);
                XSSFCell cheatingCell = row.getCell(1);
                XSSFCell violationCell = row.getCell(2);
                XSSFCell scoreCell = row.getCell(3);
                if (cellErrorValid(carNoCell, HSSFCell.CELL_TYPE_STRING) || cellErrorValid(cheatingCell, HSSFCell.CELL_TYPE_NUMERIC)
                        || cellErrorValid(violationCell, HSSFCell.CELL_TYPE_NUMERIC) || cellErrorValid(scoreCell, HSSFCell.CELL_TYPE_NUMERIC)) {
                    continue;
                }
                BlackListInsertDto insertDto = new BlackListInsertDto(carNoCell.getStringCellValue(), (int) cheatingCell.getNumericCellValue()
                        , (int) violationCell.getNumericCellValue(), null, 1, null, null, null);

                try {
                    tietouBlackListService.addOrCancelBlackList(insertDto);
                    ++successAmount;
                } catch (MyException e) {
                    log.error("加入白名单异常:{}", e.getMessage());
                }
                ++total;
            }
            successAmountDto.setSuccess(successAmount);
            successAmountDto.setTotal(total);
        } catch (IOException e) {
            log.error("{}", e);
            throw new MyException(TipsConsts.SERVER_ERROR);
        }
        return successAmountDto;
    }

    /**
     * 如果单元格为空或者参数不满足条件，返回true
     *
     * @param cell     单元格
     * @param cellType 单元格类型
     */
    private boolean cellErrorValid(XSSFCell cell, int cellType) {
        if (cell == null || cell.getCellType() != cellType) {
            return true;
        }
        return false;
    }

    /**
     * 根据车牌号获得对象
     *
     * @param carNo   车牌号
     * @param carType
     */
    public CarNoDto getOrInsertByName(String carNo, int carType) {
        if (carNo == null) {
            return null;
        }
        carNo = carNo.trim().toUpperCase();

        //查询原始表
        TietouCarDic res = thisMapper.selectByCarNoFromAll(carNo);
        if (res != null) {
            CarNoDto carNoDto = new CarNoDto();
            carNoDto.setCarNo(res.getCarNo());
            carNoDto.setId(res.getId());
            Integer id = res.getId();
            Integer carTypeDB = res.getCarType();
            if (res.getUseFlag()) {
                if (carTypeDB != null && carTypeDB < 10 && carType > 10) {
                    carNoDto = getOrInsertByNameHuo(carNo, carType, true);
                } else if (carTypeDB != null && carTypeDB > 10 && carType < 10 && carType > 0) {
                    String huoCarNo = carNo + "_货";
                    TietouCarDic carDicUpdate = new TietouCarDic();
                    carDicUpdate.setId(id);
                    carDicUpdate.setCarNo(huoCarNo);
                    thisMapper.updateCarNoById(carDicUpdate.getCarNo(), carDicUpdate.getId());
                    TietouCarDic carDicInsert = new TietouCarDic();
                    carDicInsert.setCarNo(carNo);
                    carDicInsert.setCarType(carType);
                    carDicInsert.setWhiteFlag(false);
                    carDicInsert.setUseFlag(true);
                    carDicInsert.setCreateTime(LocalDateTime.now());
                    thisMapper.insertNewCar(carDicInsert);
                    Integer oldId = id;
                    if ((id = carDicInsert.getId()) != null) {
                        BoundHashOperations<String, String, Integer> cacheOperation = redisTemplate.boundHashOps("car_cache");
                        cacheOperation.put(carNo, id);
                        cacheOperation.put(huoCarNo, oldId);
                    }
                    carNoDto.setId(id);
                    carNoDto.setCarNo(carNo);
                }
            }
            return carNoDto;
        } else {
            return getOrInsertByNameHuo(carNo, carType, false);
        }
    }

    /**
     * 在车牌后面再加入_货后缀进行查询
     *
     * @param carNo
     * @param carType
     * @param existFlag true:表示有相同车牌的客车记录，false表示没有
     * @return
     */
    private CarNoDto getOrInsertByNameHuo(String carNo, int carType, boolean existFlag) {
        CarNoDto carNoDto = new CarNoDto();
        String huoCarNo = carNo + "_货";
        //查询原始表
        TietouCarDic res = thisMapper.selectByCarNoFromAll(huoCarNo);
        if (existFlag) {
            carNo = huoCarNo;
        }
        carNoDto.setCarNo(carNo);
        if (res == null) {
            TietouCarDic carDic = new TietouCarDic();
            //判断车牌号是否合法
            carDic.setUseFlag(CarNoUtil.generateCarUseFlag(carNo));
            carDic.setCreateTime(LocalDateTime.now());
            carDic.setCarNo(carNo);
            carDic.setWhiteFlag(false);
            carDic.setCarType(carType);
            thisMapper.insertNewCar(carDic);
            Integer id = carDic.getId();
//            log.info("新增一个车牌:{}", carNo);
            BoundHashOperations<String, String, Integer> cacheOperation = redisTemplate.boundHashOps("car_cache");
            BoundHashOperations<String, String, String> cacheUselessOperation = redisTemplate.boundHashOps("car_cache_useless");
            if (id != null) {
                cacheOperation.put(carNo, id);
                if (carDic.getUseFlag()) {
                    cacheOperation.put(carNo, id);
                } else {
                    cacheUselessOperation.put(String.valueOf(id), carNo);
                }
            }
            carNoDto.setId(id);
            return carNoDto;
        } else {
            carNoDto.setId(res.getId());
            return carNoDto;
        }
    }


    /**
     * 新增一个新车牌（在新事务的情况下）
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertWithNewTransaction(TietouCarDic carDic) {
        thisMapper.insertNewCar(carDic);
    }


    public Integer selectMaxId() {
        return thisMapper.getMaxId();
    }


    public CarNoDto getOrInsertByCarNo(String carNo, Integer carType) {
        //如果是货车加上货车后缀
        if (carType > 10 && carType < 16 && !carNo.endsWith(ConfigConsts.HUO_SUFFIX)) {
            carNo += ConfigConsts.HUO_SUFFIX;
        }
        CarNoDto carNoDto = new CarNoDto();
        carNoDto.setCarNo(carNo);
        Integer id;
        TietouCarDic carDic = thisMapper.selectByCarNoFromAll(carNo);
        if (carDic == null) {
            TietouCarDic dic = new TietouCarDic();
            dic.setCreateTime(LocalDateTime.now());
            dic.setCarNo(carNo);
            dic.setCarType(carType);
            dic.setUseFlag(CarNoUtil.generateCarUseFlag(carNo));
            thisMapper.insertNewCar(dic);
            id = dic.getId();
            if (id != null) {
                log.info("新增一个车牌:{}", carNo);
                if (dic.getUseFlag()) {
                    BoundHashOperations<String, String, Integer> cacheOperation = redisTemplate.boundHashOps("car_cache");
                    cacheOperation.put(carNo, id);
                } else {
                    BoundHashOperations<String, String, String> cacheUselessOperation = redisTemplate.boundHashOps("car_cache_useless");
                    cacheUselessOperation.put(String.valueOf(id), carNo);
                }
            }
        } else {
            id = carDic.getId();
        }
        carNoDto.setId(id);
        return carNoDto;
    }
}
