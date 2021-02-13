package cn.pency.fanout;

import cn.pency.RmConst;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *类说明：
 */
@Component
public class FanoutSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send(String msg) {
        String sendMsg = msg +"---"+ System.currentTimeMillis();;
        System.out.println("FanoutSender : " + sendMsg);
        this.rabbitTemplate.convertAndSend(RmConst.EXCHANGE_FANOUT, "",sendMsg);
    }

}
