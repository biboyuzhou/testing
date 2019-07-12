package com.drcnet.highway.util.templates;

import tk.mybatis.mapper.additional.insert.InsertListMapper;
import tk.mybatis.mapper.annotation.RegisterMapper;
import tk.mybatis.mapper.common.Mapper;

//InsertListMapper<T>,, MySqlMapper<T>
@RegisterMapper
public interface MyMapper<T> extends InsertListMapper<T>, Mapper<T> {

}
