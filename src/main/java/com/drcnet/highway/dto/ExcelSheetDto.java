package com.drcnet.highway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: penghao
 * @CreateTime: 2018/12/19 10:50
 * @Description: excel 里的 sheet对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExcelSheetDto<T> {

    private String sheetName;

    private List<T> data;
}
