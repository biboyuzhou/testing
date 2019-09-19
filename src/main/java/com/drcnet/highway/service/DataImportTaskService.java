package com.drcnet.highway.service;

import com.drcnet.highway.dao.DataImportTaskMapper;
import com.drcnet.highway.dto.request.PagingDto;
import com.drcnet.highway.entity.DataImportTask;
import com.drcnet.highway.util.templates.BaseService;
import com.drcnet.highway.util.templates.MyMapper;
import com.drcnet.highway.vo.PageVo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/15 9:07
 * @Description:
 */
@Service
@Slf4j
public class DataImportTaskService implements BaseService<DataImportTask,Integer> {

    @Resource
    private DataImportTaskMapper thisMapper;

    @Override
    public MyMapper<DataImportTask> getMapper() {
        return thisMapper;
    }

    public PageVo<DataImportTask> listDataImportTaskLog(PagingDto pagingDto) {
        PageHelper.startPage(pagingDto.getPageNum(),pagingDto.getPageSize());
        List<DataImportTask> dataImportTasks = thisMapper.listDataImportTaskLog();
        return PageVo.of(dataImportTasks);
    }
}
