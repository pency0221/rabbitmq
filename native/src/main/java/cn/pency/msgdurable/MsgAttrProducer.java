package cn.pency.msgdurable;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**

 *类说明：消息的持久化生产者
 */
public class MsgAttrProducer {

    public final static String EXCHANGE_NAME = "msg_durable";

    public static void main(String[] args)
            throws IOException, TimeoutException {
        /* 创建连接,连接到RabbitMQ*/
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("127.0.0.1");
        Connection connection = connectionFactory.newConnection();

        /*创建信道*/
        Channel channel = connection.createChannel();
        //TODO 创建持久化交换器  第三个参数 durable=true
        channel.exchangeDeclare(EXCHANGE_NAME,"direct",true);

        /*日志消息级别，作为路由键使用*/
        String[] routekeys = {"king","mark","james"};
        for(int i=0;i<3;i++){
            String routekey = routekeys[i%3];
            String msg = "Hellol,RabbitMq"+(i+1);
            //TODO 发布持久化的消息 Properties中(delivery-mode=2)
            channel.basicPublish(EXCHANGE_NAME,routekey,MessageProperties.PERSISTENT_TEXT_PLAIN,msg.getBytes());
        }

        channel.close();
        connection.close();

    }

}
