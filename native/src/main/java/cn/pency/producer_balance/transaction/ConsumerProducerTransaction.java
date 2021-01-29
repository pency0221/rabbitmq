package cn.pency.producer_balance.transaction;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *类说明：消费者——事务模式 （没什么不同）
 */
public class ConsumerProducerTransaction {

    public static void main(String[] argv)
            throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");

        // 打开连接和创建频道，与发送端一样
        Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();

        channel.exchangeDeclare(ProducerTransaction.EXCHANGE_NAME,
                BuiltinExchangeType.DIRECT);

        String queueName = "producer_confirm";
        channel.queueDeclare(queueName,false,false,
                false,null);

        //只关注king的日志
        String routekey="king";
        channel.queueBind(queueName, ProducerTransaction.EXCHANGE_NAME,
                routekey);

        System.out.println(" [*] Waiting for messages......");

        // 创建队列消费者
        final Consumer consumerB = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                //记录日志到文件：
                System.out.println( "Received ["+ envelope.getRoutingKey()
                        + "] "+message);
            }
        };
        channel.basicConsume(queueName, true, consumerB);
    }

}
