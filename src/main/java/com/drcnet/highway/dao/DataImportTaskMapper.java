package com.drcnet.highway.dao;

import com.drcnet.highway.entity.DataImportTask;
import com.drcnet.highway.util.templates.MyMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataImportTaskMapper extends MyMapper<DataImportTask> {
    DataImportTask selectByMd5AndType(@Param("md5") String md5,@Param("type") Integer type);

    List<DataImportTask> listDataImportTaskLog();
}