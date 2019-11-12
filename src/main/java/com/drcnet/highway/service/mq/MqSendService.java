package com.drcnet.highway.service.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @Author jack
 * @Date: 2019/10/29 17:01
 * @Desc:
 **/
@Component
@Slf4j
public class MqSendService {

    private DefaultMQProducer producer;
    @Value("${rocketmq.namesever.addr}")
    private String mqAddr;
    @Value("${rocketmq.producer.gantryGroup}")
    private String producerGroup;


    @PostConstruct
    public void initProducer() {
        producer = new DefaultMQProducer(producerGroup);
        producer.setNamesrvAddr(mqAddr);
        producer.setRetryTimesWhenSendFailed(3);
        try {
            producer.start();
            System.out.println("[Producer 已启动]");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String topic, String tags, String messageBody, Object rount) throws Exception {
        try {
            Message message = new Message(topic, tags, null, messageBody.getBytes(RemotingHelper.DEFAULT_CHARSET));
            /*producer.send(message, new MessageQueueSelector() {
                        @Override
                        public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                            Integer id = (Integer) arg;
                            int index = id % mqs.size();
                            return mqs.get(index);
                        }
                    }, rount, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("发送成功！msgId：{}", sendResult.getMsgId());
                }

                @Override
                public void onException(Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
            * replace with lamda
            */
            producer.send(message, (mqs, msg, arg) -> {
                Integer hashCode;
                if (arg == null) {
                    hashCode = 1;
                } else {
                    hashCode = Math.abs(arg.hashCode());
                }
                int index = hashCode % mqs.size();
                return mqs.get(index);
            }, rount, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("发送成功！msgId：{}", sendResult.getMsgId());
                }

                @Override
                public void onException(Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
        } catch (Exception e) {
            log.error("发送mq出错！", e);
            throw e;
        }
    }

    @PreDestroy
    public void shutDownProducer() {
        if (producer != null) {
            producer.shutdown();
        }
    }
}
