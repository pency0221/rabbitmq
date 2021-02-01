package cn.pency.dlx;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *类说明：普通的消费者，负责消费死信队列dlx_accept的消息
 */
public class DlxProcessConsumer {

    public final static String DLX_EXCHANGE_NAME = "dlx_accept";

    public static void main(String[] argv)
            throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");

        // 打开连接和创建频道，与发送端一样
        Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();
        channel.exchangeDeclare(DLX_EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

        /*声明一个队列*/
        String queueName = "dlx_accept";
        channel.queueDeclare(queueName,false,false,
                false,null);

        /*绑定，将队列和交换器通过路由键进行绑定*/
        channel.queueBind(queueName, DLX_EXCHANGE_NAME,"#");

        System.out.println("waiting for message........");

        /*声明了一个死信消费者*/
        final Consumer consumer = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received dead letter["
                        +envelope.getRoutingKey()
                        +"]"+message);
            }
        };
        /*消费者正式开始在指定队列上消费消息*/
        channel.basicConsume(queueName,true,consumer);


    }

}
