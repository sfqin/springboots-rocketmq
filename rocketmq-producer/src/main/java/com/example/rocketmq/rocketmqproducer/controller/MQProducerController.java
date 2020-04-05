package com.example.rocketmq.rocketmqproducer.controller;

import com.example.rocketmq.rocketmqproducer.bean.MessageInfo;
import com.example.rocketmq.rocketmqproducer.bean.OrderEventInfo;
import com.example.rocketmq.rocketmqproducer.service.DefaultProducer;
import com.example.rocketmq.rocketmqproducer.service.TransactionProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class MQProducerController {

    @Autowired
    private DefaultProducer defaultProducer;

    @Autowired
    private TransactionProducer transactionProducer;

    //普通同步发送消息
    @GetMapping("/send")
    public Object sendMsg(String topic,String tag,String msg){
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setTag(tag);
        messageInfo.setTopic(topic);
        messageInfo.setText(msg);
        SendResult sendResult = defaultProducer.sendMessage(messageInfo);
        return sendResult;
    }

    //普通异步发送消息
    @GetMapping("/sendasyn")
    public Object sendAsynMsg(String topic,String tag,String msg){
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setTag(tag);
        messageInfo.setTopic(topic);
        messageInfo.setText(msg);
        defaultProducer.sendAsynMessage(messageInfo);
        return "sendResult";
    }

    // 发送延迟消息，既可以同步实现，又可以异步实现。此方法为同步实现
    @GetMapping("/sendelay")
    public Object sendDelayMsg(String topic,String tag,String msg,int delay){
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setTag(tag);
        messageInfo.setTopic(topic);
        messageInfo.setText(msg);
        messageInfo.setDelayLevel(delay);
        return defaultProducer.senddelayMessage(messageInfo);
    }

    // 发送消息到指定的queue中，既可以同步实现又可以异步实现。
    @GetMapping("/sendSelect")
    public Object sendSelectQueueMsg(String topic,String tag,String msg,int queueNum){
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setTag(tag);
        messageInfo.setTopic(topic);
        messageInfo.setText(msg);
        messageInfo.setQueueNum(queueNum);
        defaultProducer.sendSelectQueue(messageInfo);
        return "return";
    }

    // 发送消息到指定的queue中，既可以同步实现又可以异步实现。
    @GetMapping("/sendOrder")
    public Object sendOrderMsg(String topic,String tag){

        List<OrderEventInfo> list = new ArrayList<>();
        list.add(new OrderEventInfo(111,"创建订单"));
        list.add(new OrderEventInfo(222,"创建订单"));
        list.add(new OrderEventInfo(111,"支付订单"));
        list.add(new OrderEventInfo(222,"支付订单"));
        list.add(new OrderEventInfo(333,"创建订单"));
        list.add(new OrderEventInfo(111,"完成订单"));
        list.add(new OrderEventInfo(333,"支付订单"));
        list.add(new OrderEventInfo(333,"完成订单"));

        for(int i=0; i<list.size(); i++){

            OrderEventInfo orderEventInfo = list.get(i);
            MessageInfo messageInfo = new MessageInfo();
            messageInfo.setTag(tag);
            //测试指定一个 topic
            messageInfo.setTopic("orderly_topic");
            messageInfo.setText(orderEventInfo.getOrderId() +"=>" + orderEventInfo.getType());
            messageInfo.setQueueNum(orderEventInfo.getOrderId());
            defaultProducer.sendOrderQueue(messageInfo);

        }
        return "orderly";
    }


    //普通同步发送带属性的tag
    @GetMapping("/sendTag")
    public Object sendTagMsg(String topic,String tag,String msg,int tagUserId){
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setTag(tag);
        messageInfo.setTopic(topic);
        messageInfo.setText(msg);

        SendResult sendResult = defaultProducer.sendTagQueue(messageInfo,tagUserId);
        return sendResult;
    }


    //发送事务消息
    @GetMapping("/sendTra")
    public Object sendTraMsg(String topic,String tag,String msg,int param){
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setTag(tag);
        messageInfo.setTopic(topic);
        messageInfo.setText(msg);
        transactionProducer.SendTransactionMsg(messageInfo,param+"");
        return "transaction";
    }

}
