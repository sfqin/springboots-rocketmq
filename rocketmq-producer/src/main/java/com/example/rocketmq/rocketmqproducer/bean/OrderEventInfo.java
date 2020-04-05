package com.example.rocketmq.rocketmqproducer.bean;

public class OrderEventInfo {

    private int orderId;

    public OrderEventInfo(int orderId, String type) {
        this.orderId = orderId;
        this.type = type;
    }

    private String type;

    @Override
    public String toString() {
        return "OrderEventInfo{" +
                "orderId=" + orderId +
                ", type='" + type + '\'' +
                '}';
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
