package cn.pency.consumer_balance.ackfalse;

import cn.pency.exchange.direct.DirectProducer;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeoutException;

/**
 *类说明：消息者不对消息进行确认会怎样？
 */
public class AckFalseConsumerA {

    public static void main(String[] argv)
            throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");

        // 打开连接和创建频道，与发送端一样
        Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();
        channel.exchangeDeclare(DirectProducer.EXCHANGE_NAME,
                "direct");

        /*声明一个队列*/
        String queueName = "focuserror";
        channel.queueDeclare(queueName,false,false,
                false,null);

        /*绑定，将队列和交换器通过路由键进行绑定*/
        String routekey = "error";/*表示只关注error级别的日志消息*/
        channel.queueBind(queueName,DirectProducer.EXCHANGE_NAME,routekey);

        System.out.println("waiting for message........");

        /*声明了一个消费者*/
        final Consumer consumer = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                try {
                    String message = new String(body, "UTF-8");
                    System.out.println("Received["+envelope.getRoutingKey()
                            +"]"+message);
                     //TODO 业务操作...
                    //最后确认 如果中间出错 被catch捕获后可以立即拒绝确认Ack
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    //TODO 出错了 发送nAck 最后一个参数是requeue 重新投递
                    channel.basicNack(envelope.getDeliveryTag(),false,true);
                }
            }
        };
        /*消费者正式开始在指定队列上消费消息*/
        //TODO 这里第二个参数是自动确认参数，如果是false则是手动确认
        channel.basicConsume(queueName,false,consumer);


    }

}
