package cn.pency.setmsg;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 类说明：消息的属性的控制
 */
public class ReplyToConsumer {

    public static void main(String[] argv)
            throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.80.101");
        factory.setPort(5672);
        factory.setUsername("pency");
        factory.setPassword("P@ssw0rd");
        factory.setVirtualHost("pency");

        // 打开连接和创建频道，与发送端一样
        Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();
        /*创建交换器*/
        channel.exchangeDeclare(ReplyToProducer.EXCHANGE_NAME,
                "direct", false);

        /*声明一个队列*/
        String queueName = "replyto";
        channel.queueDeclare(queueName, false, false,
                false, null);

        /*绑定，将队列和交换器通过路由键进行绑定*/
        String routekey = "error";/*表示只关注error级别的日志消息*/
        channel.queueBind(queueName, ReplyToProducer.EXCHANGE_NAME, routekey);

        System.out.println("waiting for message........");

        /*声明了一个消费者*/
        final Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received[" + envelope.getRoutingKey()
                        + "]" + message);
                //TODO 从消息中拿到相关属性（确定要应答的消息ID）
                AMQP.BasicProperties respProp
                        = new AMQP.BasicProperties.Builder()
                        .replyTo(properties.getReplyTo())
                        .correlationId(properties.getMessageId())//todo 原始消息id信息
                        .build();
                //TODO 消息消费时，同时需要生作为生产者生产消息（以OK为标识）
                // 第一个参数是交换器的名字，空字符串表示它是一个默认或无命名的交换器，消息将会由指定的路由键（第二个参数，routingKey）转发到队列。
                channel.basicPublish("", respProp.getReplyTo(),respProp,
                        ("OK," + message).getBytes("UTF-8"));
            }
        };
        /*消费者正式开始在指定队列上消费消息*/
        channel.basicConsume(queueName, true, consumer);


    }

}
