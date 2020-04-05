package com.example.rocketmq.rocketmqproducer.service.impl;

import com.example.rocketmq.rocketmqproducer.bean.MessageInfo;
import com.example.rocketmq.rocketmqproducer.service.DefaultProducer;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;


@Service
public class DefaultProducerImpl implements DefaultProducer {

    private static final Logger log = LoggerFactory.getLogger(DefaultProducerImpl.class);

    private static final String defaultTopic = "default_topic";

    private static final String defaultGroup = "default_group";

    private static final String defaultTag = "";

    @Value("${rocktmq.nameserver}")
    private String nameServerAddr;

    private DefaultMQProducer mqProducer;

    @PostConstruct
    public void init() {
        mqProducer = new DefaultMQProducer(defaultGroup);
        //生产者投递消息重试次数
        mqProducer.setRetryTimesWhenSendFailed(3);
        //指定NameServer地址，多个地址以 ; 隔开
        mqProducer.setNamesrvAddr(nameServerAddr);
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
    public SendResult sendMessage(MessageInfo messageInfo) {

        log.info("发送消息为：{}", messageInfo);

        Message message = initMessage(messageInfo);
        if (message != null) {
            try {
                SendResult send = mqProducer.send(message);
                log.info("消息发送结果为：{}", send);
                return send;
            } catch (Exception e) {
                log.error("sendMessage 消息发送异常 e:{}", e);
            }

        } else {
            log.warn("sendMessage 消息内容为空");
        }

        log.info("同步消息发送完毕");
        return null;
    }

    @Override
    public void sendAsynMessage(MessageInfo messageInfo) {
        log.info("异步发送消息为：{}", messageInfo);

        Message message = initMessage(messageInfo);
        if (message != null) {

            try {
                mqProducer.send(message, new SendCallback() {

                    @Override
                    public void onSuccess(SendResult sendResult) {
                        log.info("异步消息发送结果为：{}", sendResult);
                        int a = 1 / 0;
                    }

                    @Override
                    public void onException(Throwable e) {
                        //发送失败 补偿机制，根据业务情况进行使用，看是否进行重试
                        log.error("异步消息发送失败 e:{}", e);
                    }
                });
            } catch (Exception e) {
                log.error("sendAsynMessage 消息发送异常 e:{}", e);
            }

        } else {
            log.warn("sendAsynMessage 消息内容为空");
        }


        log.info("异步消息发送完毕");
    }


    @Override
    public SendResult senddelayMessage(MessageInfo messageInfo) {
        log.info("延迟发送消息为：{}", messageInfo);

        Message message = initMessage(messageInfo);

        if (message != null) {
            //xxx是级别，1表示配置里面的第一个级别，2表示第二个级别
            //"1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h"
            //定时消息：目前rocketmq开源版本还不支持，商业版本则有，两者使用场景类似
            message.setDelayTimeLevel(messageInfo.getDelayLevel() < 0 ? 1 : messageInfo.getDelayLevel());

            try {
                SendResult send = mqProducer.send(message);
                log.info("延迟消息发送结果为：{}", send);
                return send;
            } catch (Exception e) {
                log.error("sendAsynMessage 消息发送异常 e:{}", e);
            }

        } else {
            log.warn("sendAsynMessage 消息内容为空");
        }

        log.info("延迟消息发送完毕");
        return null;
    }

    @Override
    public void sendSelectQueue(MessageInfo messageInfo) {

        log.info("sendSelectQueue 发送消息为：{}", messageInfo);

        Message message = initMessage(messageInfo);

        if (message != null) {

            try {
//                同步发送
//                SendResult send = mqProducer.send(message, new MessageQueueSelector() {
//                    /**
//                     *
//                     * @param mqs
//                     * @param msg
//                     * @param arg 为 MessageQueueSelector 传递进来的参数 messageInfo.getQueueNum()
//                     * @return
//                     */
//                    @Override
//                    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
//                        int queueNum = Integer.parseInt(arg.toString());
//                        log.info("queue 大小为 :{}, 发送到第:{}个queue", mqs.size(), queueNum);
//                        return mqs.get(queueNum);
//                    }
//                }, messageInfo.getQueueNum());
//                log.info("sendSelectQueue 消息同步发送结果：{}",send);


                /**
                 * @param arg 为 MessageQueueSelector 传递进来的参数 messageInfo.getQueueNum()
                 * 异步发送实现
                 */
                mqProducer.send(message, ((mqs, msg, arg) -> {
                    int queueNum = Integer.parseInt(arg.toString());
                    log.info("queue 大小为 :{}, 发送到第:{}个queue", mqs.size(), queueNum);
                    return mqs.get(queueNum);
                }), messageInfo.getQueueNum(), new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        log.info("sendSelectQueue 消息异步发送结果：{}", sendResult);
                    }

                    @Override
                    public void onException(Throwable e) {
                        log.error("sendSelectQueue 消息异步发送异常 e:{}", e);
                    }
                });

            } catch (Exception e) {
                log.error("sendSelectQueue 消息发送异常 e:{}", e);
            }


        } else {
            log.warn("sendSelectQueue 消息内容为空");
        }

        log.info("sendSelectQueue 发送消息完毕");
    }

    @Override
    public SendResult sendOrderQueue(MessageInfo messageInfo) {

        Message message = initMessage(messageInfo);
        SendResult send = null;
        try {
            send = mqProducer.send(message, new MessageQueueSelector() {
                /**
                 *
                 * @param mqs
                 * @param msg
                 * @param arg 为 MessageQueueSelector 传递进来的参数 messageInfo.getQueueNum()
                 * @return
                 */
                @Override
                public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                    int queueNum = (int) arg;
                    int index = queueNum % mqs.size();
                    log.info("queue 大小为 :{}, 发送到第:{}个queue,内容：{}", mqs.size(), index,new String(msg.getBody()));
                    return mqs.get(index);
                }
            }, messageInfo.getQueueNum());
        } catch (Exception e) {
            log.error("sendSelectQueue 消息发送异常 e:{}", e);
        }
        log.info("sendOrderQueue 消息同步发送结果：{}", send);

        return null;
    }

    @Override
    public SendResult sendTagQueue(MessageInfo messageInfo,int tagUserId) {

        log.info("sendTagQueue 发送消息为：{}", messageInfo);

        Message message = initMessage(messageInfo);
        if (message != null) {
            try {
                message.putUserProperty("tagUserId",tagUserId +"");
                SendResult send = mqProducer.send(message);
                log.info("sendTagQueue 消息发送结果为：{}", send);
                return send;
            } catch (Exception e) {
                log.error("sendTagQueue 消息发送异常 e:{}", e);
            }

        } else {
            log.warn("sendTagQueue 消息内容为空");
        }

        log.info("sendTagQueue 同步消息发送完毕");
        return null;
    }


    private Message initMessage(MessageInfo messageInfo) {
        if (messageInfo != null && !StringUtils.isEmpty(messageInfo.getText())) {
            Message message = new Message();
            message.setBody(messageInfo.getText().getBytes());
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
            return message;
        }
        return null;
    }


}
