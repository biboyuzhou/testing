package com.drcnet.highway.util.templates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

/**
*
* @author lin
* @create  22:34 2018/5/23
**/
public interface BaseService<T extends Serializable, P extends Serializable> {

    Logger logger = LoggerFactory.getLogger(BaseService.class);

    /**
     * 根据主键查询
     */
    default T selectByPrimaryKey(P id){
        return getMapper().selectByPrimaryKey(id);
    }

    /**
     * 查询一个
     */
    default T selectOne(T record){
        return getMapper().selectOne(record);
    }

    /**
     * 根据条件查询
     */
    default List<T> selectByRecord(T record){
        return getMapper().select(record);
    }

    /**
     * 查询所有
     */
    default List<T> selectAll(){
        return getMapper().selectAll();
    }

    /**
     * 添加
     */
    default int insert(T record){
        return getMapper().insert(record);
    }

    default int insertSelective(T record){
        return getMapper().insertSelective(record);
    }
    /**
     * 根据主键修改
     */
    default int updateByPrimaryKey(T record){
        return getMapper().updateByPrimaryKeySelective(record);
    }

    /**
     * 根据主键删除
     */
    default int deleteByPrimaryKey(P id){
        return getMapper().deleteByPrimaryKey(id);
    }

    default int insertAll(List<T> dataList){
        return getMapper().insertList(dataList);
    }

    MyMapper<T> getMapper();

}
