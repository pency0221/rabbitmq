###Exchange
####direct交换器
1.原生java客户端使用direct交换器 一般用法

2.原生java客户端使用direct交换器 队列绑定多个路由键用法
 * >一个队列 使用多个路由键与交换器绑定 这些路由键路由的消息都会路由到这个队列上
   
3.原生java客户端使用direct交换器 一个Connection可以创建多个channal 多个队列绑定这同一个交换器 不同消费者消费演示
* >个人误区注意点：即使是direct交换器,如果有多个队列使用符合这个消息的绑定键绑定了同一个交换器,那么这条消息也会被direct交换器都转发到这几个队列上去,符合规则就人人有份,并不会采取什么轮训啊啥的这次发这个下次再发另外一个队列（这是队列和多个消费者的策略之一 别混喽）  
也就意味这即使direct交换器 消费方发送的消息个数 和经过交换器转发给对列的总数量也并不是一定一致。

4.原生java客户端使用direct交换器 一个队列多个消费者
* >则会表现出消息在消费者之间的轮询接收
  
####Fanout 广播交换器
原生java客户端使用Fanout交换器 和绑定键无关
* >Fanout广播交换器 消息传递和绑定键无关 只要队列绑定了这个交换器 都能收到fanout交换器全部消息
  
####**Topic 主题交换器**
原生java客户端使用Topic主题订阅交换器
topic 主题交换器,主题订阅 通过路由键组合匹配 路由键是通过.分割关键字的字符串 其中可以用#和*通配

>\#和*的区别:  
>- \#可以匹配0或多层单词  
>- *只能匹配一个单词

假设现在路由键的规则为 “老师.技术专题.课程章节"，如：king.kafka.A  
则队列queueName订阅TopicProducer.EXCHANGE_NAME的消息：channel.queueBind(queueName,TopicProducer.EXCHANGE_NAME, "#.B");  
订阅内容与之相匹配的路由绑定键规则如下:
>- 订阅所有老师的所有课程的所有章节   “#”
>- 订阅所有老师的所有课程的B章节     “#.B” 
>- 订阅King老师的所有课程的B章节     “king.#” 
>- 订阅所有kafka课程的所有章节      “ \*.kafka.* “
>- 订阅king老师的kafka课程的A章节   “king.kafka.A“
>- 订阅订阅king老师的所有课程的A章节 “king.*.A“

topic的灵活之处体现在：无论是direct交换器还是fanout交换器的功能 它都能模拟代替
  - 当一个队列以”#”作为绑定键时,它将接收所有消息,而不管路由键如何,类似于fanout型交换器
  - 当特殊字符”*”、”#”没有用到绑定时，topic型交换器就好比direct型交换器了
  
###消息发布时的可靠性与性能权衡
####失败通知
无任何保障的发送消息给rabbitmq，如果这个消息无法被交换器路由(没有对应的队列),那么生产者也不会知道，(如果交换器也没加其他策略)相当于消息被路由器丢了。如果生产者想知道发送的消息是否可路由到队列，在发送消息时可以设置 mandatory 标志，告诉 RabbitMQ，如果消息不可路由，应该将消息返回给发送者，并通知失败。
```
//TODO 路由失败通知 回调
channel.addReturnListener(new ReturnListener() {
   public void handleReturn(int replycode, String replyText, String exchange, String routeKey, AMQP.BasicProperties basicProperties, byte[] bytes) throws IOException {
       String message = new String(bytes);
       System.out.println("返回的replycode:"+replycode);//错误码
       System.out.println("返回的replyText:"+replyText);//错误提示
       System.out.println("返回的exchange信息:"+exchange);
       System.out.println("返回的routeKey信息:"+routeKey);
       System.out.println("返回的消息:"+message);
   }
});
......
//TODO 第三个参数 就是mandatory标志位   true 确保可路由 失败检查 不可路由时回调ReturnListener的handleReturn
 channel.basicPublish(EXCHANGE_NAME,routekey,true,null,message.getBytes());
```
####事务
（由于性能影响较大，基本不开启）  
事务的实现主要是对信道（Channel）的设置，主要的方法有三个：
> 1.channel.txSelect()声明启动事务模式  
> 2.channel.txComment()提交事务  
> 3.channel.txRollback()回滚事务 
####发送方确认
由于事务影响性能较大，一种比较好的替代方式是开启发送方确认(confirm模式)  
使用步骤：  
1、发送方通道开启confirm模式：
```
//启用发送者确认模式
channel.confirmSelect();
```
2、basicPublish发送完消息之后 选择确认方式三种之一确认rabbitmq是否收到：
>- 方式一(同步单条确认)：channel.waitForConfirms()普通发送方确认模式；消息到达交换器，就会返回 true。  
>- 方式二(同步批量确认)：channel.waitForConfirms()批量确认模式；使用同步方式等所有的消息发送之后才会返回确认执行后面代码，只要有一个消息未到达交换器就会抛出 IOException 异常。
>- 方式三(异步确认)：channel.addConfirmListener()异步监听发送方确认模式；是否会批量rabbitmq内部自己平衡 混合模式  
由于对于不可路由的消息 confirm模式也会返回ack  确认收到，所以单纯的只使用confirm确认模式并不能保证是否路由到队列上，所以confirm模式一般与mandatory失败确认(可路由检查)联合使用。  

失败通知（mandatory）和发送方确认（confirm）区别辨析:  

- 失败通知只针对消息是否可路由
- 发送方确认主要针对的是出现内部错误异常时队列没收到消息的情况，对于消息不可路由的情况 rabbitmq依然响应ack
####备用交换器
在第一次声明交换器时被指定，用来提供一种预先存在的交换器，如果主交换器无法路由消息，那么消息将被路由到这个新的备用交换器  
如果发布消息时同时设置了mandatory，因为主交换器指定了备交换器，当主交换器无法路由消息，这时候主交换器会把消息发送到备用交换器，RabbitMQ并不会通知发布者，因为消息有地方去了。  
备用交换器就是普通的交换器，它也有对应绑定它的队列,没有任何特殊的地方。

###消息发布时的可靠性与性能权衡
####消息的获得方式
- get方式(不推荐)： 消费方在一个死循环中不断的get尝试取消息  要不断的和rabbitmq发生网络通讯 性能差 
```       
 while(true){
    //拉一条，自动确认的(rabbit 认为这条消息消费 -- 从队列中删除)
    GetResponse getResponse = channel.basicGet(queueName, ture);
    ...
}
```
- 推送Consume方式 ： 最常用的方式 就是rabbitmq队列主动推送投递给消费者   
```channel.basicConsume(queueName,false,consumer);```
####消息的应答（自动确认和手动确认）
- 自动确认：投递完就删除  
消费时指定参数autoAck为true即可：  
  channel.basicConsume(queueName, true, consumerA);//第二个参数就是 autoAck
- 手动确认：做完业务之后在对消息进行确认让rabbitmq对消息删除  
  channel.basicConsume(queueName, false, consumerA);令autoAck=false，消费者就有足够的时间处理消息(任务)，不用担心处理消息过程中消费者进程挂掉后消息丢失的问题，因为 RabbitMQ 会一直持有消息直到消费者显式调用 channel.basicAck为止。  
  >等待投递的消息状态为ready  
  >投递完ready->unchecked 待确认  
  >消费方手动确认显式调用basicAck rabbitmq删除消息  
  >如果到消费方断开连接还是unchecked未确认状态则有unchecked->ready 等待重新投递
####Qos预取模式  
相当于分批处理。这一批没确认不投递给下一批。  
RabbitMQ提供了一种QOS（Quality of Service ，服务质量保证）功能，即在非自动确认消息的前提下，如果一定数目的消息（通过基于consume或者channel设置QOS的值）未被确认前，不进行消费新的消息，这样起到流控和保证质量的作用。  
```
//TODO 设置预取模式 150条预取(150都取出来 150， 210-150  60  ) 第二个参数 global代表channel级别限制  否则 消费者级别
channel.basicQos(150,true);
//TODO 使用预取前提 关闭自动确认 autoAck=false 默认消费者一般单条确认
channel.basicConsume(queueName,false,consumer);
```
//消费者接受到消息业务操作之后单条确认 或分批确认 
```
//TODO 单条确认
channel.basicAck(envelope.getDeliveryTag(),false);
//TODO 批量确认
if(meesageCount %50 ==0){
   this.getChannel().basicAck(envelope.getDeliveryTag(),true);
    System.out.println("批量消息费进行消息的确认------------");
}
```
