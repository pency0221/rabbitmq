package cn.pency.setmsg;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**

 *类说明：消息的属性的控制
 */
public class ReplyToProducer {

    public final static String EXCHANGE_NAME = "replyto";

    public static void main(String[] args)
            throws IOException, TimeoutException {
        /* 创建连接,连接到RabbitMQ*/
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("192.168.80.101");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("pency");
        connectionFactory.setPassword("P@ssw0rd");
        connectionFactory.setVirtualHost("pency");
        Connection connection = connectionFactory.newConnection();

        /*创建信道*/
        Channel channel = connection.createChannel();
        /*创建持久化交换器*/
        channel.exchangeDeclare(EXCHANGE_NAME,"direct",false);

        //TODO 响应QueueName,消费者通过这个信息将会把要返回的信息发送到该Queue
        String responseQueue = channel.queueDeclare().getQueue();
        String msgId = UUID.randomUUID().toString();
        //TODO 设置消息中的应答属性
        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                .replyTo(responseQueue) //todo 执行消费端收到消息后 如果响应 响应到哪个队列上去
                .messageId(msgId)
                .build();

        String msg = "Hello,RabbitMq";
        //TODO 发送消息时，把响应相关属性设置进去
        channel.basicPublish(EXCHANGE_NAME,"error", properties,msg.getBytes());
        System.out.println("Sent error:"+msg);

        /*TODO 同时声明了一个消费者用于接收响应消息*/
        final Consumer consumer = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                //todo 原始消息的id
                String correlationId = properties.getCorrelationId();
                System.out.println("Received["+envelope.getRoutingKey()+"]"+message+",原始消息id："+correlationId);
            }
        };
        //TODO 消费者应答队列上的消息
        channel.basicConsume(responseQueue,true,consumer);
    }

}
