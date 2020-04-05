package com.example.rocketmq.rocketmqproducer.service.mq;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;

@Component
public class MyTransactionServiceImpl implements TransactionListener {

    //执行本地事务
    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        //调用第三方 操作数据库，如果全部执行成功则返回成功
        System.out.println("执行本地事务 msg:" + msg);
        System.out.println("执行本地事务 arg:" + arg);
        int status = Integer.parseInt(arg.toString());

        //二次确认消息，然后消费者可以消费
        if(status == 1){
            return LocalTransactionState.COMMIT_MESSAGE;
        }

        //回滚消息，broker端会删除半消息
        if(status == 2){
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }

        //broker端会进行回查消息，再或者什么都不响应
        if(status == 3){
            return LocalTransactionState.UNKNOW;
        }

        return null;
    }


    /**
     * 回查消息，要么commit 要么rollback，reconsumeTimes不生效
     * 防止上面执行本地事务返回的状态 网络异常broker未接收到，所以会主动发送请求来查询事务执行状态
     * @param msg
     * @return
     */
    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {

        System.out.println("本地事务回查 msg:"+msg);

        //要么commit 要么rollback
        //可以根据key去检查本地事务消息是否完成
        String body = new String(msg.getBody());
        String key = msg.getKeys();
        String transactionId = msg.getTransactionId();
        System.out.println("transactionId="+transactionId+", key="+key+", body="+body);

        return LocalTransactionState.COMMIT_MESSAGE;
    }



}
