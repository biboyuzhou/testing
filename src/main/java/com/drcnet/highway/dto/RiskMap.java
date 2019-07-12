package com.drcnet.highway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/10 16:16
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskMap {

    //数字code
    private Integer code;
    //中文
    private String msg;
    //数量
    private Integer amount;

    public RiskMap(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
