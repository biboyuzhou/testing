package com.drcnet.highway.service.external;

import com.alibaba.fastjson.JSON;
import com.drcnet.highway.dto.request.enternal.GantryDataDto;
import com.drcnet.highway.service.mq.MqSendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author jack
 * @Date: 2019/10/29 17:35
 * @Desc:
 **/
@Service
@Slf4j
public class GantryDataService {
    @Resource
    private MqSendService mqSendService;

    @Value("${rocketmq.topic.gantryData}")
    private String topic;


    public void processGantryData(GantryDataDto gantryDataDto) {
        String body = JSON.toJSONString(gantryDataDto);
        try {
            mqSendService.sendMessage(topic, null, body, gantryDataDto.getPlateNum());
        } catch (Exception e) {
            log.error("发送消息到mq失败！", e);
            //TODO增加失败处理流程
        }

    }
}
