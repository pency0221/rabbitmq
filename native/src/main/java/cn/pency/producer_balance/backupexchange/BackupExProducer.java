package cn.pency.producer_balance.backupexchange;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 *类说明：生产者--绑定了一个备用交换器
 */
public class BackupExProducer {

    public final static String EXCHANGE_NAME = "main-exchange";
    public final static String BAK_EXCHANGE_NAME = "ae";

    public static void main(String[] args)
            throws IOException, TimeoutException {
        /**
         * 创建连接连接到RabbitMQ
         */
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");

        // 创建一个连接
        Connection connection = factory.newConnection();
        // 创建一个信道
        Channel channel = connection.createChannel();
        //TODO 构建参数map   参数alternate-exchange是固定的 表示的就是备用交换器的 key
        Map<String,Object> argsMap = new HashMap<String,Object>();
        argsMap.put("alternate-exchange",BAK_EXCHANGE_NAME);
        //TODO 声明主交换器时 最后一个传入带有备用交换器信息的map 指定它的备用交换器名
        channel.exchangeDeclare(EXCHANGE_NAME,"direct",false,false,argsMap);
        //有消息不可路由到队列也不会再调用 只要这些消息可以被路由到备用交换器
        channel.addReturnListener(new ReturnListener() {
            @Override
            public void handleReturn(int i, String s, String s1, String s2, AMQP.BasicProperties basicProperties, byte[] bytes) throws IOException {
            }
        });

        //TODO 声明备用交换器 一般直接声明为FANOUT类型  主交换器无法路由的消息会被路由到备用交换器上让它在路由绑定自己的队列上去。
        channel.exchangeDeclare(BAK_EXCHANGE_NAME,BuiltinExchangeType.FANOUT,true,false,null);

        //所有的消息
        String[] routekeys={"king","mark","james"};
        for(int i=0;i<3;i++){
            //每一次发送一条不同老师的消息
            String routekey = routekeys[i%3];
            // 发送的消息
            String message = "Hello World_"+(i+1);
            //参数1：exchange name //参数2：routing key
            // TODO 即使第三个参数mandatory ture开启了失败确认 主交换器上有没有可路由的队列的消息也不会再次回调ReturnListener.handleReturn 因为可“路由”到备交换器
            channel.basicPublish(EXCHANGE_NAME, routekey,true,null, message.getBytes());
            System.out.println(" [x] Sent '" + routekey +"':'" + message + "'");
        }
        // 关闭频道和连接
        channel.close();
        connection.close();
    }

}
