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
####路由失败通知
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

路由失败通知（mandatory）和发送方确认（confirm）区别辨析:  

- 路由失败通知只针对消息是否可路由
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
####消费端处理消息使用事务 
使用方法和生产者中使用一样，略。  
注意点:
- 手动确认模式：消息如果开启了事务，即使已经手动确认(发送了baseAck) rabbitmq 也不会删除 要等到事务提交后删除 出错回滚 （可以理解为 手动确认模式 开始事务 baseAck命令失效）
- 自动确认模式：不支持开启事务 事务失效 直接删了出错也回滚不了  
####消息的拒绝  
消费者处理确认处理消息回应ack外，还可以拒绝消息，当消费者处理消息失败或者当前不能处理该消息时，可以给Broker发送一个拒绝消息的指令，并且可以要求Broker将该消息丢弃或者重新放入消息队列中。  
Reject 和 Nack
都能指定拒绝后rabbitmq是否requeue重新投递还是丢弃(拒绝后需要重新投递的会根据轮询规则投递给下一个消费者)  
区别是:
- reject只能拒绝单条
- Nack可以批量拒绝    
```
    try{
     //消费者取到消息 业务处理过程出错或显示拒绝
        String message = new String(body, "UTF-8");
       //...
        throw new RuntimeException("处理异常"+message);
    }catch (Exception e){
        e.printStackTrace();
        //TODO Reject方式拒绝(这里第2个参数requeue决定是否重新投递)
        //channel.basicReject(envelope.getDeliveryTag(),true);
        //TODO Nack方式的拒绝（第2个参数决定是否批量） 第三个参数requeue
        channel.basicNack(envelope.getDeliveryTag(), false, true);
    }
```  
####DLX死信交换器  
对于一些被拒绝且requeue=false不重新投递的消息、队列达到最大长度被迫出队队首的消息、长期存在队列上未消费过期的消息(如果设置了过期时间)这些属于"死信",顾明思议 没被正常消费 “死了”的消息。  
rabbitMQ作为一个高级消息中间件,提出了死信交换器的概念来专门处理死了的信息（被拒绝可以重新投递的信息不能算死的）。  
死信交换器是RabbitMQ 对 AMQP 规范的一个扩展，往往用在对问题消息的诊断上（主要针对消费者），还有延时队列（延时处理消息）的功能。一般用在秒单场景（一般用于秒单场景 比如需要记录秒单失败被拒绝的原始消息，或延时处理）  
消息变成死信一般是以下三种情况：
- 消息被拒绝，并且设置 requeue 参数为 false
- 消息过期（默认情况下 Rabbit 中的消息不过期，但是可以设置队列的过期时间和消息的过期时间以达到消息过期的效果）
- 队列达到最大长度（一般当设置了最大队列长度或大小并达到最大值时）

正常的消息队列绑定处理它的死信的死信交换器:
```
    //TODO 声明死信交换器
    channel.exchangeDeclare(DlxProcessConsumer.DLX_EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
    //TODO 绑定死信交换器 x-dead-letter-exchange参数是固定的 值为死信交换器的名字
    /*声明一个队列，并绑定死信交换器*/
    args.put("x-dead-letter-exchange", DlxProcessConsumer.DLX_EXCHANGE_NAME);
    //TODO 还可以指定死信路由键，消息覆盖原来的路由键后发送给死信交换器
    args.put("x-dead-letter-routing-key", "deal");
    //TODO 声明正常消息的队列时 通过参数map绑定它的死信交换器
    channel.queueDeclare(queueName,false,true,false,args);
```  
死信交换器和备用交换器的区别:  
- 1、备用交换器主要用于生产者端，发布到rabbitmq之前，是主交换器无法路由消息，那么消息将被路由到这个新的备用交换器    
而死信交换器主要在消费者端，则是接收过期或者被拒绝的消息。  
- 2、备用交换器是在声明主交换器时发生联系，而死信交换器则声明队列时发生联系。  
场景分析：备用交换器一般是用于生产者生产消息时，确保消息可以尽量进入 RabbitMQ,而死信交换器主要是用于消费者消费消息的万无一失性的场景（比如消息过期，队列满了，消息拒绝且不重新投递）  
###队列相关  
####临时队列
临时队列：临时队列只是个代名词，以下的自动删除队列、单消费者队列、自动过期队列都是临时队列的一种。  
- 自动删除队列   autoDelete=true  
当这个队列上最后一个消费者(channel)断开连接后自动删除这个队列。  
- 单消费者队列(即排他)   exclusive=true  
设置exclusive排他的作用：只能有一个消费者（channel）消费这个队列的消息，**并且这个消费者断开之后这个队列还会自动删除**来防止其他消费者消费。  
本质是只有这个channel（一般是创建它的）可以访问它（加锁了），其他channel访问报错拒绝。  
> Caused by: com.rabbitmq.client.ShutdownSignalException: channel error; protocol method: #method<channel.close>(reply-code=405, reply-text=RESOURCE_LOCKED - cannot obtain exclusive access to locked queue 'queue-king' in vhost 'pency', class-id=50, method-id=10)  
- 自动过期队列  通过x-expires参数设定   
可以把自动过期队列理解成自动删除队列的加强版，可以删除一定时间内都“没有被使用”的队列”。  
“没有被使用”的条件：  
1. 一定时间对它没有 Get 操作发生。  
2. 一定时间这个队列上没有 Consumer 与它保持连接（自创建后一直没消费者"订阅"它或已"订阅"的消费者不在一定时间后没有任何消费者重连）。  
注意：就算一直有消息进入队列也不算队列在被使用。  
     即使这个队列上没有任何消息，只要绑定它的消费者还在就也算在被使用（即使没有在消费消息）  
#####各种临时队列用法总结:  
排他、自动删除队列创建的相关入参：   
//durable持久化  exclusive排他(单一消费者)  autoDelete自动删除  
queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)  
```
//比如排他单消费者队列
 channel.queueDeclare(queueName,false,true, false,null);
```
自动过期队列的创建：
```
String queueName = "setQueue";
Map<String, Object> arguments = new HashMap<String, Object>();
arguments.put("x-expires",10*1000);//断开10秒后被删除
//其他参数...
channel.queueDeclare(queueName,false,false, false,arguments);
...
```  
####永久队列（持久化） 
持久化队列：持久化队列会被持久化到磁盘里，即使重启rabbitmq服务器队列信息也不会丢失.（这里只是队列持久 非消息）  
方法:  
//声明队列时第二个参数 durable 传true  
channel.queueDeclare(queueName,true,false, false,arguments);  
#####扩展：
以上只是队列的持久化，而想要达到消息的持久化,除了队列需要持久化外 生产者端还需满足创建时指定交换器、发布的消息、队列都持久化才可以：  
```
//TODO 创建持久化交换器  第三个参数 durable=true
channel.exchangeDeclare(EXCHANGE_NAME,"direct",true);
...
//TODO 发布持久化的消息 Properties中(delivery-mode=2)
channel.basicPublish(EXCHANGE_NAME,routekey,MessageProperties.PERSISTENT_TEXT_PLAIN,msg.getBytes());
```  
####队列级别消息过期  
就是为每个队列设置消息的超时时间。只要给队列设置 x-message-ttl 参数，就设定了该队列所有消息的存活时间，时间单位是毫秒。消息到时间没被消费就成了"死信"。有死信交换器就处理，没有就被丢弃  
```
String queueName = "setQueue";
Map<String, Object> arguments = new HashMap<String, Object>();
arguments.put("x-message-ttl",40*1000);//消息40秒没被消费成为"死信"。有死信交换器就处理，没有就被丢弃
//其他参数...
channel.queueDeclare(queueName,false,false, false,arguments);
```  
###消息的属性  
RabbitMQ 对消息进行了如下标准化：  
按照 AMQP 的协议单个最大的消息大小为 16GB（2 的 64 次方），但是 RabbitMQ 将消息大小限定为 2GB（2 的 31 次方）。  
发布消息时同时发布消息头帧（属性），是消息的描述：
> //消息头帧对应的消就是息发布时的第三个参数BasicProperties：  
> public void basicPublish(String exchange, String routingKey, BasicProperties props, byte[] body)  
 
Basic.Properties的各个基本属性：  
- content-type: 内容体的类型，如application/json
- content-encoding: 压缩或编码格式
- message-id和correlation-id: 唯一标识消息和消息响应，用于工作流程中实现消息跟踪
- timestamp: 减少消息大小，描述消息创建时间
- expiration: 表明消息过期
- delivery-mode: 将消息写入磁盘或内存队列
- app-id和user-id: 帮助追踪出现问题的消息发布者应用程序
- type: 定义消息类型的自由格式字符串值
- reply-to: 实现响应消息的路由
- headers: 是一个映射表，定义自由格式的属性和实现rabbitmq路由    
BasicProperties对象的构建可以使用AMQP提供了建造者模式构建：  
```
AMQP.BasicProperties respProp  
        = new AMQP.BasicProperties.Builder()  
        .replyTo(properties.getReplyTo())  
        .correlationId(properties.getMessageId())
        //.xxx....  
        .build();
channel.basicPublish(EXCHANGE_NAME,routekey,respProp,msg.getBytes());
```  
####消息存活时间TTL  
队列设置消息的 TTL是声明队列是通过“x-message-ttl” 参数设置的，这个队列中消息的ttl都是一样的。  
发送时指定消息的TTL是通过消息头帧消息属性expiration指定的。  
####消息的持久化  
默认情况下，队列和交换器在服务器重启后都会消失，消息当然也是。  
将队列和交换器的 durable 属性设为 true，缺省为 false，但是消息要持久化还不够，还需要将消息在发布前，将投递模式设置为 2。  
总结：消息要持久化,必须要有持久化的队列、交换器的持久化和消息投递模式delivery-mode为2(消息持久化)。  
####Request-Response 模式  
Mq一般的模式中都是一方负责发送消息而另外一方负责处理。而实际中的很多应用相当于一种一应一答的过程，需要双方都能给对方发送消息。于是请求-应答的这种通信方式也很重要。它也应用的很普遍。  
主要流程：相当于生产者通过队列发送给消费者消息后 消费者通过另外一个响应队列再给原先的生产者回应一个消息(也可以理解成两方都既是生产者又是消费者) 通过消息属性的message-id和correlation-id做原始消息和响应消息的关联。  

