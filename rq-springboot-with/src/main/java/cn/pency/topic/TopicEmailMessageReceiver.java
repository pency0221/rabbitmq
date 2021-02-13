package cn.pency.topic;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**

 *类说明：
 */
@Component
@RabbitListener(queues = "sb.info.email")
public class TopicEmailMessageReceiver {

    @RabbitHandler
    public void process(String msg) {
        System.out.println("TopicEmailMessageReceiver  : " +msg);
    }

}
