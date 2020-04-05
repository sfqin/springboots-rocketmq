package com.example.rocketmq.rocketmqproducer.service;

import com.example.rocketmq.rocketmqproducer.bean.MessageInfo;

public interface TransactionProducer {

    /**
     * 发送事务消息
     * @param messageInfo
     * @param param
     */
    void SendTransactionMsg(MessageInfo messageInfo, String param);

}
