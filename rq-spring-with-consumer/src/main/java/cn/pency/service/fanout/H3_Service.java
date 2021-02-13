package cn.pency.service.fanout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

/**

 *类说明：
 */
public class H3_Service implements MessageListener{
    private Logger logger = LoggerFactory.getLogger(H3_Service.class);
    public void onMessage(Message message) {
        logger.info("Get message: "+new String( message.getBody()));
    }
}
