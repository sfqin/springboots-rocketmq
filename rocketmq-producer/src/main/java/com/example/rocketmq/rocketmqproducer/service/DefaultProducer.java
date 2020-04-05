package com.example.rocketmq.rocketmqproducer.service;

import com.example.rocketmq.rocketmqproducer.bean.MessageInfo;
import org.apache.rocketmq.client.producer.SendResult;

public interface DefaultProducer {


    /**
     * 发送一个最普通的消息 同步发送
     * @param messageInfo
     * @return
     */
    SendResult sendMessage(MessageInfo messageInfo);

    /**
     * 异步发送消息 消费端无序发生改变
     * @param messageInfo
     */
    void sendAsynMessage(MessageInfo messageInfo);

    /**
     * 发送延迟消息 同步发送
     * @param messageInfo
     * @return
     */
    SendResult senddelayMessage(MessageInfo messageInfo);

    /**
     * 发送消息 到指定的topic的queue下
     * @param messageInfo
     */
    void sendSelectQueue(MessageInfo messageInfo);

    /**
     * 发送顺序消息 到指定的topic的queue下
     * @param messageInfo
     * @return
     */
    SendResult sendOrderQueue(MessageInfo messageInfo);

    /**
     * 指定tag的属性发送消息
     * @param messageInfo
     * @return
     */
    SendResult sendTagQueue(MessageInfo messageInfo,int tagUserId);

}
