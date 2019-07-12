package com.drcnet.highway.vo;

import com.drcnet.highway.exception.InternalServerErrorException;
import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageVo<T> implements Serializable {

    private static final long serialVersionUID = 7117172980850934548L;
    private long total;

    private List<T> data;

    private Object detail;

    public static <T> PageVo<T> of(PageInfo<T> pageInfo){
        if (pageInfo == null){
            log.error("pageInfo is null");
            throw new InternalServerErrorException("参数异常");
        }
        return new PageVo<>(pageInfo.getTotal(),pageInfo.getList());
    }

    public static <T> PageVo<T> of(List<T> dataList){
        if (dataList == null){
            log.error("pageInfo is null");
            throw new InternalServerErrorException("参数异常");
        }
        return of(new PageInfo<>(dataList));
    }

    public PageVo(long total, List<T> data) {
        this.total = total;
        this.data = data;
    }
}
