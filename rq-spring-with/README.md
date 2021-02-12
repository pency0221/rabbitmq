##与spring的集成  
生产者、发送者项目pom中引入Spring-rabbit依赖包  
**基础配置:**  
1.统一配置（生产者消费者都需要配置）:也就是rabbitmq连接工厂配置信息(rabbitConnectionFactory) 一般使用CachingConnectionFactory类  

2.生产者端  
配置：RabbitTemplate、queue、exchange(以及binding队列信息)  
代码:生产者端业务类注入rabbitTemplate 使用rabbitTemplate.send发送消息即可 消息属性可以自由设置MessageProperties。  

3.消费者端  
配置：
- 队列、exchange(以及binding队列信息)  
- 消费者bean：就是实现了MessageListener接口的普通bean，重写onMessage方法即可（消费消息的方法）。  
  既然是bean，它的配置采用Spirng两种bean配置方式之一就行：xml bean配置、包扫描方式（component-scan+@component）  
  
- 监听容器：listener-container，消费者端特有配置，用来绑定队列和消费者，指明哪个队列上的消息被哪个消费者（的哪个方法）消费

代码：在消费者bean的onMessage方法内写消费消息的业务逻辑。

#### 高级配置： 
生产者端常配置：  
1. 路由失败通知mandatory的回调:rabbittemplate配置中开启mandatory不可路由通知及指明回调处理类，回调处理类的特点是实现了ReturnCallback接口。  
2. 发送者确认通知confirm的回调:和mandatory路由失败通知配置相似，也是在rabbittemplate指明回调处理类，区别是需要在配置CachingConnectionFactory中开启，接收ack/nack的处理类实现的是ConfirmCallback接口。  

消费者端常配置：  
1. 手动确认：  
配置：消费者bean和队列的绑定关系是在监听容器listener-container中配置的，所以消息消费者收到的确认方式也在这里配置，只需要设置acknowledge（“告知已收到“的方式）为manual（“手动的”）即可,acknowledge="manual"。  
代码改动：消费者要手动确认就必须要拿到channel对象，而消费者实现的简单的消息监听接口MessageListener只能拿到消息，拿不到channel，所以开启了手动消息确认，消费者bean就需要改变继承的接口为功能更强大能拿到channel的ChannelAwareMessageListener接口，
然后调用channel.basicAck方法对消息手动确认。  
   
2. Qos
限制消费者预取数量，保证消息消费确认质量，在listener-container中配置prefetch，prefetch="50",当这个消费者有50个消息没有ack之前不再投递给它，即消费者粒度限制了最大unacked的数量。  
   
   




  

