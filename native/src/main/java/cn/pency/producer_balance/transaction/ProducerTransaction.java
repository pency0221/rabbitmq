package cn.pency.producer_balance.transaction;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *类说明：生产者—— 开启事务 路由不到队列回滚
 * 性能影响大 一般不会开启
 */
public class ProducerTransaction {

    public final static String EXCHANGE_NAME = "producer_transaction";

    public static void main(String[] args)
            throws IOException, TimeoutException, InterruptedException {
        /**
         * 创建连接连接到RabbitMQ
         */
        ConnectionFactory factory = new ConnectionFactory();

        // 设置MabbitMQ所在主机ip或者主机名
        factory.setHost("127.0.0.1");
        // 创建一个连接
        Connection connection = factory.newConnection();
        // 创建一个信道
        Channel channel = connection.createChannel();
        // 指定转发
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

        String[] routekeys={"king","mark","james"};
        //TODO 信道开启事务
        channel.txSelect();
        try {
            for(int i=0;i<3;i++){
                String routekey = routekeys[i%3];
                // 发送的消息
                String message = "Hello World_"+(i+1)
                        +("_"+System.currentTimeMillis());
                channel.basicPublish(EXCHANGE_NAME, routekey, true,
                        null, message.getBytes());
                System.out.println("----------------------------------");
                System.out.println(" Sent Message: [" + routekey +"]:'"
                        + message + "'");
                Thread.sleep(200);
            }
            //TODO 事务提交
            channel.txCommit();
        } catch (IOException e) {
            e.printStackTrace();
            //TODO 事务回滚
            channel.txRollback();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 关闭频道和连接
        channel.close();
        connection.close();
    }


}
