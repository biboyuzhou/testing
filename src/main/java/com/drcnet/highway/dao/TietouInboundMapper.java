package com.drcnet.highway.dao;

import com.drcnet.highway.dto.request.ChangeCardQueryDto;
import com.drcnet.highway.dto.response.ChangeCardResponse;
import com.drcnet.highway.entity.TietouInbound;
import com.drcnet.highway.util.templates.MyMapper;

import java.util.List;

public interface TietouInboundMapper extends MyMapper<TietouInbound> {
    /**
     * 查询换卡风险数据
     * @param dto
     * @return
     */
    List<ChangeCardResponse> listChangeCardList(ChangeCardQueryDto dto);

    int insertIgnore(TietouInbound inbound);
}