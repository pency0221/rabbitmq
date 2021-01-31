package cn.pency.consumer_balance.getmessage;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *类说明：消费者——拉取模式
 */
public class GetMessageConsumer {


    public static void main(String[] argv)
            throws IOException, TimeoutException, InterruptedException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");

        // 打开连接和创建频道，与发送端一样
        Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();

        channel.exchangeDeclare(GetMessageProducer.EXCHANGE_NAME,"direct");
        // 声明一个队列
        String queueName = "focuserror";
        channel.queueDeclare(queueName,false,false,false,null);

        String routekey="error";//只关注error级别的日志，然后记录到文件中去。
        channel.queueBind(queueName, GetMessageProducer.EXCHANGE_NAME, routekey);

        System.out.println(" [*] Waiting for messages......");
        //TODO 无限循环拉取
        while(true){
            //拉一条，自动确认的(rabbit 认为这条消息消费 -- 从队列中删除)
            GetResponse getResponse = channel.basicGet(queueName, false);
            if(null!=getResponse){
                System.out.println("received[" +getResponse.getEnvelope().getRoutingKey()+"]"
                        +new String(getResponse.getBody()));
            }
            //确认(自动、手动)
            channel.basicAck(0,true);
            Thread.sleep(1000);
        }


    }

}
