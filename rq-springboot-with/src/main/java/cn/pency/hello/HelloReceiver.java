package cn.pency.hello;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 *类说明：
 */
@Component
@RabbitListener(queues = "sb.hello")
public class HelloReceiver {
    @RabbitHandler
    public void process(String hello) {
        System.out.println("HelloReceiver : " + hello);
    }
}
