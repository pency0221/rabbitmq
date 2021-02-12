##与spring的集成  
生产者、发送者项目pom中引入Spring-rabbit依赖包  
**基础配置:**  
统一配置（生产者消费者都需要配置）:也就是rabbitmq连接工厂配置信息(rabbitConnectionFactory) 一般使用CachingConnectionFactory类 

生产者端：RabbitTemplate、queue、exchange(以及binding队列信息)  
生产者端代码:生产者端业务类注入rabbitTemplate 使用rabbitTemplate.send发送消息即可 消息属性可以只有设置MessageProperties。  

消费者端配置：
- 队列、exchange(以及binding队列信息)  
- 消费者bean：就是实现了MessageListener接口的普通bean，重写onMessage方法即可（消费消息的方法）。  
  既然是bean，它的配置采用Spirng两种bean配置方式之一就行：xml bean配置、包扫描方式（component-scan+@component）  
  
- 监听容器：listener-container，消费者端特有配置，用来绑定队列和消费者，指明哪个队列上的消息被哪个消费者（的哪个方法）消费  


