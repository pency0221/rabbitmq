package cn.pency.callback;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * @author pency
 * 类说明：失败通知的回调
 */
@Component
public class SendReturnCallback implements RabbitTemplate.ReturnCallback {
    public void returnedMessage(Message message, int replyCode,
                                String replyText, String exchange,
                                String routingKey) {
        String msg = new String(message.getBody());
        System.out.println("返回的replyText ："+replyText);
        System.out.println("返回的exchange ："+exchange);
        System.out.println("返回的routingKey ："+routingKey);
        System.out.println("返回的message ："+message);

    }
}
