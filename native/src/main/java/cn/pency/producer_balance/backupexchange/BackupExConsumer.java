package cn.pency.producer_balance.backupexchange;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *类说明：绑定备用交换器队列的消费者
 */
public class BackupExConsumer {


    public static void main(String[] argv)
            throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");

        // 打开连接和创建频道，与发送端一样
        Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();
        channel.exchangeDeclare(BackupExProducer.BAK_EXCHANGE_NAME,BuiltinExchangeType.FANOUT,true, false, null);
        // 声明一个队列
        String queueName = "fetchother";
        channel.queueDeclare(queueName,false,false,false,null);

        channel.queueBind(queueName,
                BackupExProducer.BAK_EXCHANGE_NAME, "#");

        System.out.println(" [*] Waiting for messages......");

        // 创建队列消费者
        final Consumer consumerB = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag,Envelope envelope,AMQP.BasicProperties properties,byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                //记录日志到文件：
                System.out.println( "Received ["+ envelope.getRoutingKey() + "] "+message);
            }
        };
        channel.basicConsume(queueName, true, consumerB);
    }

}
