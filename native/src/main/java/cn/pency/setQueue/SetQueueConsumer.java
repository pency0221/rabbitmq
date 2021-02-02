package cn.pency.setQueue;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**

 *类说明：控制队列的参数
 */
public class SetQueueConsumer {
    public final static String EXCHANGE_NAME = "set_queue";

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
        channel.exchangeDeclare(EXCHANGE_NAME,"direct");

        //TODO /*自动过期队列--参数需要Map传递*/
        String queueName = "setQueue";
        Map<String, Object> arguments = new HashMap<String, Object>();
        arguments.put("x-expires",10*1000);//10秒被删除
        //加入队列的各种参数...
        //durable持久化(false临时队列)   exclusive排他(单一消费者)  autoDelete自动删除
        /*queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)*/
        channel.queueDeclare(queueName,false,false, false,arguments);

        /*绑定，将队列和交换器通过路由键进行绑定*/
        String routekey = "error";/*表示只关注error级别的日志消息*/
        channel.queueBind(queueName,EXCHANGE_NAME,routekey);

        System.out.println("waiting for message........");
        /*声明了一个消费者*/
        final Consumer consumer = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag,Envelope envelope,AMQP.BasicProperties properties,byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received["+envelope.getRoutingKey()+"]"+message);
            }
        };
        /*消费者正式开始在指定队列上消费消息*/
         channel.basicConsume(queueName,true,consumer);


    }

}
