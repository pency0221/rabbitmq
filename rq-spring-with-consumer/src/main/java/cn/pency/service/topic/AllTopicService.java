package cn.pency.service.topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.stereotype.Component;

/**

 *类说明：订阅所有的消息
 */
@Component
public class AllTopicService implements MessageListener {
    private Logger logger = LoggerFactory.getLogger(AllTopicService.class);
    public void onMessage(Message message) {
        logger.info("Get message: "+new String( message.getBody()));
    }
}
