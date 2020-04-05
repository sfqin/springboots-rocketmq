# springboots-rocketmq
rocketmq学习

#### 消息发送重试处理办法
1. 生产者Producer重试（异步和SendOneWay下配置无效）。即消息重投，默认重试次数是2，可修改
2. 消费端重试。消息处理异常，或者broker端到consumer端网络等各种问题，导致消费失败。重试间隔时间配置，默认每条消息最多重试16次。messageDelayLevel=1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h。超过次数后需要人工补偿。所以在消费端可能存在消费重复的消息。消费端需要对消息进行去重处理。另外需要注意的是，目前消费重试只针对集群消费方式有效，暂不支持方波方式重试。

#### 消息发送模式
1. ONEWAY : 无须等待响应，也没有回调。使用场景logServer抓取日志
2. SYNC ：同步发送，可靠性高，性能相对低一些。使用场景，营销，重要邮件通知等。
3. ASYNC：异步发送，快，切可靠性高。比如注册账号后，同时增加几分服务等。

#### 顺序消息（消息的生产和消费顺序一致）
- 全局顺序：topic下面全部消息都要有序(少用)。性能低，严格按照FIFO进行消息发布与消费
- 局部有序：保持一组消息被顺序消费即可。即一组消息在一个topic 的同一个queue中。例如 电商的订单创建，同一个订单相关的创建订单消息、订单支付消息、订单退款消息、订单物流消息、订单交易成功消息 都会按照先后顺序来发布和消费
- 顺序发布：对于指定的一个 Topic，客户端将按照一定的先后顺序发送消息
- 顺序消费：对于指定的一个 Topic，按照一定的先后顺序接收消息，即先发送的消息一定会先被客户端接收到。
- 1.顺序消息暂不支持广播模式。2.顺序消息不支持异步发送方式，否则将无法严格保证顺序
- 问题，如何保证 consumer 实例始终绑定在某个queue上

#### 集群模式和广播模式
- 集群模式。Consumer实例平均分摊消费生产者发送的消息，一个消息只被消费一次。
- 广播模式。每个Consumer实例都会消费生产者投递到broker中的消息。
- 切换模式。consumer.setMessageModel() 默认是集群模式

#### tag过滤方式
- Broker端过滤，减少了无用的消息的进行网络传输，但是增加了broker的负担
- Consumer端过滤，根据自身业务需求取出相应的消息，但是增加了很多无用的消息传输

#### 消息消费方式
- push 推。服务端主动将消息推送到consumer端。效率高，实时性高。但是增加了服务端的负载。为提高push方式的效率，push同样可以做缓存，攒够一定的数量再一次性发送。
- pull 拉。consumer端主动去服务端拉取消息。主动权在客户端，可控性好。但是拉取的时间间隔需要控制好，不能太短（空拉，浪费资源）也不能太长（消息不能及时消费），有一种方式就是长轮询，拉一次保活15s。
- PushConsumer 本质还是使用pull的方式实现的。 使用PullConsumer 需要自己维护Offset，处理不同状态的消息 FOUND、NO_NEW_MSG、OFFSET_ILLRGL、NO_MATCHED_MSG、4种状态。灵活性好，编码相对较复杂。主要是释放资源和保存Offset。

#### 生产环境推荐配置
- nameServer 配置多个不同节点
- 多个Master，每个Master带有一个Slave
- 主从设置为同步双写 SYN_MASTER
- Producer 同步方式投递 broker
- 持久化策略采用异步刷盘SYNC_FLUSH

### 故障演练，主从，模式宕机后，消息还能消费

#### 实现了分布式事务