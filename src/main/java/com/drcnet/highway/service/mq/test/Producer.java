package com.drcnet.highway.service.mq.test;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;

/**
 * @Author jack
 * @Date: 2019/10/29 10:06
 * @Desc:
 **/
@Component
public class Producer {
    private static int count = 100;

    public static void main(String[] args) throws Exception {
        // 设置生产者组名
        DefaultMQProducer producer = new DefaultMQProducer("testGroup");
        // 指定nameServer的地址
        producer.setNamesrvAddr("192.168.101.204:9876");
        // 启动实例
        producer.start();


        final Semaphore semaphore = new Semaphore(0);

        for (int i = 0; i < count; i++) {
            Thread.sleep(3000);
            Message message = new Message("TopicTest",
                    "test_tag",
                    ("Hello RocketMQ " + i).getBytes(RemotingHelper.DEFAULT_CHARSET));

            producer.send(message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    System.out.println(String.format("message [%s] send success!", new String(message.getBody())));
                    semaphore.release();
                }

                @Override
                public void onException(Throwable throwable) {
                    throwable.printStackTrace();
                }
            });

        }
        semaphore.acquire(count);
        //关闭生产者，释放资源
        producer.shutdown();

    }
}
