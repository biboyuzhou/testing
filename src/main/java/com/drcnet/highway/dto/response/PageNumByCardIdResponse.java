package com.drcnet.highway.dto.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author jack
 * @Date: 2019/8/16 9:31
 * @Desc:
 **/
@Data
public class PageNumByCardIdResponse implements Serializable {

    private static final long serialVersionUID = 8579585507225673935L;

    /**
     * 数据对应的页码
     */
    private Integer pageNum;

    /**
     * 数据对应的行程记录id
     */
    private Integer recordId;
}
