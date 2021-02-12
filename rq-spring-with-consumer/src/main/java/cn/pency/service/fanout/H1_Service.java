package cn.pency.service.fanout;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;

/**
 *类说明：
 *  XxxAware.. 实现它 就能拿到Xxx
 */
public class H1_Service implements ChannelAwareMessageListener {
    private Logger logger = LoggerFactory.getLogger(H1_Service.class);
    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        logger.info("Get message: "+new String( message.getBody()));
        //todo 业务逻辑...

         //todo 手动确认收到消息 （通知rabbitmq可以删除消息了）
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),
                false);
    }
}
