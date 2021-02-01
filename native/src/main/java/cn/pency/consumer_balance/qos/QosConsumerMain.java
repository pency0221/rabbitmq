package cn.pency.consumer_balance.qos;

import cn.pency.exchange.direct.DirectProducer;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**

 *类说明：普通的消费者
 */
public class QosConsumerMain {

    public static void main(String[] argv)
            throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");

        // 打开连接和创建频道，与发送端一样
        Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();
        channel.exchangeDeclare(DirectProducer.EXCHANGE_NAME,"direct");

        /*声明一个队列*/
        String queueName = "focuserror";
        channel.queueDeclare(queueName,false,false,false,null);

        /*绑定，将队列和交换器通过路由键进行绑定*/
        String routekey = "error";/*表示只关注error级别的日志消息*/
        channel.queueBind(queueName,DirectProducer.EXCHANGE_NAME,routekey);

        System.out.println("waiting for message........");

        /*声明了一个消费者*/
        final Consumer consumer = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag,Envelope envelope, AMQP.BasicProperties properties,byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received["+envelope.getRoutingKey()+"]"+message);
                //TODO 单条确认 这里multiple设置成false/true区别不大（虽然true为批量确认<=DeliveryTag的） 因为无论如何这行代码都会执行 都会确认
                channel.basicAck(envelope.getDeliveryTag(),false);
            }
        };

        //TODO 设置预取模式 150条预取(150都取出来 150， 210-150  60  ) 第二个参数 global代表channel级别限制  否则 消费者级别
        channel.basicQos(150,true);
        //TODO 使用预取前提 关闭自动确认 autoAck=false 默认消费者一般单条确认
        channel.basicConsume(queueName,false,consumer);

        //TODO 可以自定义消费者批量确认
        //BatchAckConsumer batchAckConsumer = new BatchAckConsumer(channel);
        //channel.basicConsume(queueName,false,batchAckConsumer);


    }

}
