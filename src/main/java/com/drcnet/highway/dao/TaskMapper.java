package com.drcnet.highway.dao;

import com.drcnet.highway.entity.Task;
import com.drcnet.highway.util.templates.MyMapper;

import java.util.List;

public interface TaskMapper extends MyMapper<Task> {
    List<Task> selectUnFinishTask();
}