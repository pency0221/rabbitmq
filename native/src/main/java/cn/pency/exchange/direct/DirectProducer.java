package cn.pency.exchange.direct;



import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *类说明：direct类型交换器的生产者
 */
public class DirectProducer {

    public final static String EXCHANGE_NAME = "direct_2";

    public static void main(String[] args)
            throws IOException, TimeoutException {
        //创建连接、连接到RabbitMQ
        ConnectionFactory connectionFactory= new ConnectionFactory();
        //设置下连接工厂的连接地址(使用默认端口5672)
        connectionFactory.setHost("192.168.80.101");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("pency");
        connectionFactory.setPassword("P@ssw0rd");
        connectionFactory.setVirtualHost("pency");
        //创建连接
        Connection connection =connectionFactory.newConnection();
        //创建信道
        Channel channel =connection.createChannel();

        //在信道中设置交换器
        channel.exchangeDeclare(EXCHANGE_NAME,BuiltinExchangeType.DIRECT);

        //申明队列（放在消费者中去做）
        //申明路由键\消息体
        String[] routeKeys ={"king","mark","james"};
        for (int i=0;i<6;i++){
            String routeKey = routeKeys[i%3];
            String msg = "Hello,RabbitMQ"+(i+1);
            //发布消息
            channel.basicPublish(EXCHANGE_NAME,routeKey,null,msg.getBytes());
            System.out.println("Sent:"+routeKey+":"+msg);
        }
        channel.close();
        connection.close();

    }

}
