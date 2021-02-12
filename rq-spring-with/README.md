##与spring的集成  
生产者、发送者项目pom中引入Spring-rabbit依赖包  
**基础配置:**  
统一配置（生产者消费者都需要配置）:也就是rabbitmq连接工厂配置信息(rabbitConnectionFactory) 一般使用CachingConnectionFactory类 

生产者端：RabbitTemplate、queue、exchange(以及binding队列信息)  
生产者端代码:生产者端业务类注入rabbitTemplate 使用rabbitTemplate.send发送消息即可 消息属性可以只有设置MessageProperties。  
