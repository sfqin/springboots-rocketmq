package com.example.rocketmq.rocketmqproducer.bean;

public class MessageInfo {

    private String topic;

    private String tag;

    private String text;

    // 延迟消息的延迟等级
    private int delayLevel;

    // 指定的queueNum 默认queue的大小为4，取值只能为 0,1,2,3
    private int queueNum;

    @Override
    public String toString() {
        return "MessageInfo{" +
                "topic='" + topic + '\'' +
                ", tag='" + tag + '\'' +
                ", text='" + text + '\'' +
                ", delayLevel=" + delayLevel +
                '}';
    }

    public int getQueueNum() {
        return queueNum;
    }

    public void setQueueNum(int queueNum) {
        this.queueNum = queueNum;
    }

    public int getDelayLevel() {
        return delayLevel;
    }

    public void setDelayLevel(int delayLevel) {
        this.delayLevel = delayLevel;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }



}
