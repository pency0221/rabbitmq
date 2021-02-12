package cn.pency.service.topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.stereotype.Component;

/**

 *类说明：
 */
@Component
public class AllKafkaTopicService implements MessageListener {
    private Logger logger = LoggerFactory.getLogger(AllKafkaTopicService.class);
    public void onMessage(Message message) {
        logger.info("Get message: "+new String( message.getBody()));
    }
}
