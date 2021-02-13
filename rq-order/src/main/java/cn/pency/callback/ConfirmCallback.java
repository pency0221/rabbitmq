package cn.pency.callback;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.stereotype.Component;

/**
 * @author pency
 * 类说明：发送者确认的回调
 */
@Component
public class ConfirmCallback implements RabbitTemplate.ConfirmCallback {
    public void confirm(CorrelationData correlationData,
                        boolean ack,
                        String cause) {
        if(ack){
            System.out.println("消息发送给mq成功");
        }else{
            System.out.println("消息发送给mq失败，原因："+cause);
        }

    }
}
