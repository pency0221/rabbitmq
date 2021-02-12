package cn.pency.dlx;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**

 *类说明：普通的消费者,但是自己无法消费的消息，将投入死信队列
 * 从队列声明时就指定它上面的死信消息转发到哪个死信交换器上
 */
public class WillMakeDlxConsumer {

    public static void main(String[] argv)
            throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");

        // 打开连接和创建频道，与发送端一样
        Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();
        channel.exchangeDeclare(DlxProducer.EXCHANGE_NAME,BuiltinExchangeType.TOPIC);

        String queueName = "dlx_make";
        Map<String,Object> args = new HashMap<String,Object>();
        //TODO 声明死信交换器
        channel.exchangeDeclare(DlxProcessConsumer.DLX_EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        //TODO 绑定死信交换器 x-dead-letter-exchange参数是固定的 值为死信交换器的名字
        /*声明一个队列，并绑定死信交换器*/
        args.put("x-dead-letter-exchange", DlxProcessConsumer.DLX_EXCHANGE_NAME);
        //TODO 还可以指定死信路由键，消息覆盖原来的路由键后发送给死信交换器
        args.put("x-dead-letter-routing-key", "deal");
        //TODO 声明正常消息的队列时 通过参数map指定它的死信交换器
        channel.queueDeclare(queueName,false,true,false,args);

        /*绑定，将队列和交换器通过路由键进行绑定*/
        channel.queueBind(queueName,DlxProducer.EXCHANGE_NAME,"#");

        System.out.println("waiting for message........");

        /*声明了一个消费者*/
        final Consumer consumer = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                //TODO 确认一部分消息
                if(envelope.getRoutingKey().equals("king")){
                    System.out.println("Received[" +envelope.getRoutingKey()+"]"+message);
                    channel.basicAck(envelope.getDeliveryTag(),false);
                }else{
                    //TODO 其他的消息拒绝（并且queue=false），这些消息将成为死信消息
                    channel.basicReject(envelope.getDeliveryTag(),false);
                    System.out.println("Will reject["+envelope.getRoutingKey() +"]"+message);
                }

            }
        };
        /*消费者正式开始在指定队列上消费消息*/
        channel.basicConsume(queueName,false,consumer);

    }

}
