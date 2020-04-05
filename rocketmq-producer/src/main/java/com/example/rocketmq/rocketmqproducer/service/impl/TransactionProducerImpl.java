package com.example.rocketmq.rocketmqproducer.service.impl;

import com.example.rocketmq.rocketmqproducer.bean.MessageInfo;
import com.example.rocketmq.rocketmqproducer.service.TransactionProducer;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.*;

@Service
public class TransactionProducerImpl implements TransactionProducer {

    private static final Logger log = LoggerFactory.getLogger(TransactionProducerImpl.class);

    @Autowired
    private TransactionListener transactionListener;

    private static final String defaultTopic = "transaction_topic";

    private static final String defaultGroup = "transaction_group";

    private static final String defaultTag = "";

    @Value("${rocktmq.nameserver}")
    private String nameServerAddr;

    private TransactionMQProducer mqProducer;

    //一般自定义线程池的时候，需要给线程加个名称
    private ExecutorService executorService = new ThreadPoolExecutor(2, 5, 100, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(2000), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("client-transaction-msg-check-thread");
            return thread;
        }
    });

    @PostConstruct
    public void init() {
        mqProducer = new TransactionMQProducer(defaultGroup);
        //生产者投递消息重试次数
        mqProducer.setRetryTimesWhenSendFailed(3);
        //指定NameServer地址，多个地址以 ; 隔开
        mqProducer.setNamesrvAddr(nameServerAddr);
        mqProducer.setTransactionListener(transactionListener);
        mqProducer.setExecutorService(executorService);
        try {
            mqProducer.start();
        } catch (MQClientException e) {
            log.error("rocketmq start error: {}", e);
        }
    }

    @PreDestroy
    public void shutDown() {
        this.mqProducer.shutdown();
    }

    @Override
    public void SendTransactionMsg(MessageInfo messageInfo,String param) {
        Message message = new Message();
        message.setBody(messageInfo.getText().getBytes());
        message.setKeys(param+"-key");
        if (StringUtils.isEmpty(messageInfo.getTopic())) {
            message.setTopic(defaultTopic);
        } else {
            message.setTopic(messageInfo.getTopic());
        }
        if (StringUtils.isEmpty(messageInfo.getTag())) {
            message.setTags(defaultTag);
        } else {
            message.setTags(messageInfo.getTag());
        }

        try {
            TransactionSendResult result = mqProducer.sendMessageInTransaction(message, param);

            log.info("SendTransactionMsg 发送消息结果：{}",result);
        } catch (MQClientException e) {
            log.error("SendTransactionMsg 发送消息异常");
        }

    }
}
