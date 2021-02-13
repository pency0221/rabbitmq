package cn.pency.mirrorqueue;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**

 *类说明：普通的消费者
 */
public class MirrrorConsumer {

    public static void main(String[] argv)
            throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.56.101");
        factory.setUsername("king");
        factory.setPassword("123456");

        // 打开连接和创建频道，与发送端一样
        Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();
        channel.exchangeDeclare(MirrorProducer.EXCHANGE_NAME,
                "fanout");
        //todo 镜像队列
        /*声明一个队列*/
        String queueName = "mirror_queue_test";
        Map<String, Object> arguments = new HashMap<String, Object>();
        arguments.put("x-ha-policy","all");
        channel.queueDeclare(queueName,false,false,
                false,arguments);

        /*绑定，将队列和交换器通过路由键进行绑定*/
        String routekey = "king";/*表示只关注king级别的日志消息*/
        channel.queueBind(queueName,MirrorProducer.EXCHANGE_NAME,routekey);

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
