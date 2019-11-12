package com.drcnet.highway.service.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.drcnet.highway.constants.EsIndexConsts;
import com.drcnet.highway.domain.es.EsGantryData;
import com.drcnet.highway.dto.request.enternal.GantryDataDto;
import com.drcnet.highway.service.es.EsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Author jack
 * @Date: 2019/10/29 17:43
 * @Desc:
 **/
@Component
@Slf4j
public class GantryDataConsumer {

    @Value("${rocketmq.consumer.gantryGroup}")
    private String consumerGroup;
    @Value("${rocketmq.namesever.addr}")
    private String namesrvAddr;
    @Value("${rocketmq.topic.gantryData}")
    private String topic;

    @Resource
    private EsService esService;

    @PostConstruct
    public void defaultMQPushConsumer() {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroup);
        consumer.setNamesrvAddr(namesrvAddr);
        try {
            consumer.subscribe(topic, "*");

            // 如果是第一次启动，从队列头部开始消费
            // 如果不是第一次启动，从上次消费的位置继续消费
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);

            /*consumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                                ConsumeConcurrentlyContext context) {
                    MessageExt messageExt = msgs.get(0);
                    System.out.println(String.format("Custome message [%s],tagName[%s]",
                            new String(messageExt.getBody()),
                            messageExt.getTags()));
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });*/

            consumer.registerMessageListener((MessageListenerConcurrently) (list, context) -> {
                try {
                    BulkRequest bulkRequest = new BulkRequest();
                    int count = 0;
                    for (MessageExt messageExt : list) {
                        String messageBody = new String(messageExt.getBody(), RemotingHelper.DEFAULT_CHARSET);
                        GantryDataDto gantryDataDto = JSON.parseObject(messageBody, GantryDataDto.class);
                        log.info("收到车牌消息为：{}", gantryDataDto.getPlateNum());
                        pushMessage2Db(gantryDataDto, bulkRequest, messageExt.getMsgId());
                        count++;
                        if (count % 1000 == 0 || count == list.size()) {
                            esService.batchAddData(bulkRequest);
                            bulkRequest = new BulkRequest();
                        }
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                } catch (Exception e) {
                    e.printStackTrace();
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            });
            consumer.start();
            System.out.println("[Consumer 已启动]");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pushMessage2Db(GantryDataDto gantryDataDto, BulkRequest bulkRequest, String msgId) {
        EsGantryData esGantryData = new EsGantryData();
        BeanUtils.copyProperties(gantryDataDto, esGantryData);
        LocalDateTime localDateTime = LocalDateTime.parse(gantryDataDto.getDate(), DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
        localDateTime = LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonth(), localDateTime.getDayOfMonth(), localDateTime.getHour(), localDateTime.getMinute(), 0);
        esGantryData.setSendDate(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(localDateTime));

        IndexRequest request = new IndexRequest(EsIndexConsts.GANTRY_DATA);
        String doc = JSONObject.toJSONString(esGantryData, SerializerFeature.WriteDateUseDateFormat);
        request.id(msgId);
        request.source(doc, XContentType.JSON);
        //esService.addData(request);
        bulkRequest.add(request);
    }

}
