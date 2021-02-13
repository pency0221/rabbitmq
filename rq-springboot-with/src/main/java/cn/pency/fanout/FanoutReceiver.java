package cn.pency.fanout;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**

 *类说明：
 */
@Component
@RabbitListener(queues = "sb.fanout.A")
public class FanoutReceiver {

    @RabbitHandler
    public void process(String hello) {
        System.out.println("FanoutReceiver : " + hello);
    }

}
