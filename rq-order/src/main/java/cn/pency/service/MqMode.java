package cn.pency.service;

import cn.pency.vo.GoodTransferVo;
import com.google.gson.Gson;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 *类说明：消息发送到rabbitMQ
 */
@Service
@Qualifier("mq")
public class MqMode  implements IProDepot {

    private final static String DEPOT_RK = "amount.depot";
    private final static String DEPOT_EXCHANGE = "depot-amount-exchange";

    @Autowired
    RabbitTemplate rabbitTemplate;

    private static Gson gson = new Gson();
    //TODO 订单消息发送至RabbitMQ
    public void processDepot(String goodsId, int amount) {
        GoodTransferVo goodTransferVo = new GoodTransferVo();
        goodTransferVo.setGoodsId(goodsId);
        goodTransferVo.setChangeAmount(amount);
        goodTransferVo.setInOrOut(false);
        String goods = gson.toJson(goodTransferVo);
        MessageProperties messageProperties = new MessageProperties();
        //todo 发送消息的时候
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        rabbitTemplate.send(DEPOT_EXCHANGE, DEPOT_RK,
                new Message(goods.getBytes(), messageProperties));
//        rabbitTemplate.send(DEPOT_EXCHANGE, "ErrorRoute",
//                new Message(goods.getBytes(), messageProperties));
    }
}
