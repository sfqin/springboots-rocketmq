package com.example.rocketmqconsumer.service;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
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
public class TagMqConsumer {

    private static final Logger log = LoggerFactory.getLogger(DefaultMqConsumer.class);

    private static final String defaultTopic = "tag_topic";

    private static final String defaultGroup = "tag_consumer_group";

    private static final String defaultTag = "";

    @Value("${rocktmq.nameserver}")
    private String nameServerAddr;


    @PostConstruct
    public void getMsg() throws MQClientException {

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(defaultGroup);

        consumer.setNamesrvAddr(nameServerAddr);

        //首次启动从什么位置开始消费消息
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);

        try {
            //订阅全部标签
            //consumer.subscribe(defaultTopic,"*");
            //订阅单个标签
            //consumer.subscribe(defaultTopic,"tagA");
            //多标签订阅
            //consumer.subscribe(defaultTopic,"tagA || tagB");
            //根据sql语法对属性值进行过滤消息
            consumer.subscribe(defaultTopic, MessageSelector.bySql(" tagUserId > 5 "));
        } catch (MQClientException e) {
            log.error("消息接收异常 e:{}",e);
        }

        consumer.registerMessageListener(new MessageListenerConcurrently() {

            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {

                log.info("消息体为：{}",msgs);

                MessageExt messageExt = msgs.get(0);

                log.info("线程：{} 收到消息：{}",Thread.currentThread().getName(),new String(messageExt.getBody()));

                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        consumer.start();

        log.info("tag_topic consumer start...");
    }

}
