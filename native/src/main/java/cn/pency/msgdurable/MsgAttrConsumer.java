package cn.pency.msgdurable;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *类说明：消息持久化的消费者
 */
public class MsgAttrConsumer {

    public static void main(String[] argv)
            throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");

        // 打开连接和创建频道，与发送端一样
        Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();
        /*创建持久化交换器*/
        channel.exchangeDeclare(MsgAttrProducer.EXCHANGE_NAME,"direct",true);

         //TODO 声明一个持久化队列(durable=true)
        String queueName = "msgdurable";
        channel.queueDeclare(queueName,true,false,false,null);

        /*绑定，将队列和交换器通过路由键进行绑定*/
        String routekey = "king";/*表示只关注king的消息*/
        channel.queueBind(queueName,MsgAttrProducer.EXCHANGE_NAME,routekey);

        System.out.println("waiting for message........");

        /*声明了一个消费者*/
        final Consumer consumer = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received["+envelope.getRoutingKey()
                        +"]"+message);
            }
        };
        /*消费者正式开始在指定队列上消费消息*/
        channel.basicConsume(queueName,true,consumer);


    }

}
