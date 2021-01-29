package cn.pency.producer_balance.producerconfirm;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *类说明：生产者——发送方确认模式--异步监听确认
 */
public class ProducerConfirmAsync {

    public final static String EXCHANGE_NAME = "producer_async_confirm";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        /**
         * 创建连接连接到MabbitMQ
         */
        ConnectionFactory factory = new ConnectionFactory();

        // 设置MabbitMQ所在主机ip或者主机名
        factory.setHost("192.168.80.101");
        factory.setPort(5672);
        factory.setUsername("pency");
        factory.setPassword("P@ssw0rd");
        factory.setVirtualHost("pency");
        // 创建一个连接
        Connection connection = factory.newConnection();
        // 创建一个信道
        Channel channel = connection.createChannel();
        // 指定转发
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        //TODO
        // 1、启用发送者确认模式
        channel.confirmSelect();
        //TODO
        // 2、添加发送者确认监听器
        channel.addConfirmListener(new ConfirmListener() {
            //TODO 成功   deliveryTag 投递号       multiple是否多条 批量
            public void handleAck(long deliveryTag, boolean multiple)
                    throws IOException {
                System.out.println("send_ACK:"+deliveryTag+",multiple:"+multiple);
            }
            //TODO 失败
            public void handleNack(long deliveryTag, boolean multiple)
                    throws IOException {
                System.out.println("Erro----send_NACK:"+deliveryTag+",multiple:"+multiple);
            }
        });

        //TODO
        // 添加失败者通知
        channel.addReturnListener(new ReturnListener() {
            public void handleReturn(int replyCode, String replyText,
                                     String exchange, String routingKey,
                                     AMQP.BasicProperties properties,
                                     byte[] body)
                    throws IOException {
                String message = new String(body);
                System.out.println("RabbitMq路由失败:  "+routingKey+"."+message);
            }
        });


        String[] routekeys={"king","mark"};
        //TODO 6条
        for(int i=0;i<20;i++){
            String routekey = routekeys[i%2];
            //String routekey = "king";
            // 发送的消息
            String message = "Hello World_"+(i+1)+("_"+System.currentTimeMillis());
            channel.basicPublish(EXCHANGE_NAME, routekey, true,
                    MessageProperties.PERSISTENT_BASIC, message.getBytes());
        }
        // 关闭频道和连接
        //channel.close();
        //connection.close();
    }


}
