package com.drcnet.highway.service;

import com.drcnet.highway.dao.TaskMapper;
import com.drcnet.highway.entity.Task;
import com.drcnet.highway.util.templates.BaseService;
import com.drcnet.highway.util.templates.MyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/12 17:08
 * @Description:
 */
@Service
@Slf4j
public class TaskService implements BaseService<Task,Integer> {

    @Resource
    private TaskMapper taskMapper;

    @Override
    public MyMapper<Task> getMapper() {
        return taskMapper;
    }

    /**
     * 更新数据计算状态
     * @param taskId
     * @param state
     */
    public int updateTaskState(Integer taskId, Integer state) {
        Task task = new Task();
        task.setId(taskId);
        task.setState(state);
        task.setFinishTime(new Date());
        return taskMapper.updateByPrimaryKeySelective(task);
    }

    @Transactional(rollbackFor = Exception.class)
    public void getNewTaskId(Integer id) {
        Task task = taskMapper.selectByPrimaryKey(id);
        if (task == null) {
            updateTaskState(1, 9);
            task = new Task();
            task.setId(id);
            task.setTaskType(1);
            task.setCreateTime(new Date());
            int taskId = taskMapper.insert(task);
            logger.info("task insert into success! taskId: {}", taskId);
        }

    }
}
