package com.example.rocketmqconsumer.service;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.*;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class OrderlyMqConsumer {


    private static final Logger log = LoggerFactory.getLogger(OrderlyMqConsumer.class);

    private static final String defaultTopic = "orderly_topic";

    private static final String defaultGroup = "orderly_consumer_group";

    private static final String defaultTag = "";

    @Value("${rocktmq.nameserver}")
    private String nameServerAddr;


    /**
     * 启动三个不同的实例 会分别从 nameserver 拿到特定的queue 监听
     * @throws MQClientException
     */
    @PostConstruct
    public void getMsg() throws MQClientException {

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(defaultGroup);

        consumer.setNamesrvAddr(nameServerAddr);

        //首次启动从什么位置开始消费消息
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);

        try {
            consumer.subscribe(defaultTopic,"*");
        } catch (MQClientException e) {
            log.error("消息接收异常 e:{}",e);
        }

        consumer.registerMessageListener(new MessageListenerOrderly() {

            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
                MessageExt messageExt = msgs.get(0);

                System.out.printf("线程：%s 收到消息：%s\n",Thread.currentThread().getName(),new String(messageExt.getBody()));

                return ConsumeOrderlyStatus.SUCCESS;
            }
        });

        consumer.start();

        log.info("orderly_topic consumer start...");
    }

}
