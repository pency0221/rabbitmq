package cn.pency.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *类说明：spring生产者，模拟两种消息发送，一种fanout、一种topic
 * todo 对rabbitmq操作 如发送消息都直接通过rabbitTemplate
 */
@Controller
@RequestMapping("/rabbitmq")
public class RabbitMqController {

    private Logger logger = LoggerFactory.getLogger(RabbitMqController.class);
    //TODO
    @Autowired
    RabbitTemplate rabbitTemplate;

    @ResponseBody
    @RequestMapping("/fanoutSender")
    public String fanoutSender(@RequestParam("message")String message){
        String opt="";
        try {
            for(int i=0;i<3;i++){
                String str = "Fanout,the message_"+i+" is : "+message;
                logger.info("**************************Send Message:["+str+"]");
                //todo  生产者发送消息
                rabbitTemplate.send("fanout-exchange","",
                        new Message(str.getBytes(),new MessageProperties()));
            }
            opt = "suc";
        } catch (Exception e) {
            opt = e.getCause().toString();
        }
        return opt;
    }

    @ResponseBody
    @RequestMapping("/topicSender")
    public String topicSender(@RequestParam("message")String message){
        String opt="";
        try {
            String[] routekeys={"king","mark","james"};
            String[] modules={"kafka","jvm","redis"};
            for(int i=0;i<routekeys.length;i++){
                for(int j=0;j<modules.length;j++){
                    String routeKey = routekeys[i]+"."+modules[j];
                    String str = "Topic,the message_["+i+","+j+"] is [rk:"+routeKey+"][msg:"+message+"]";
                    logger.info("**************************Send Message:["+str+"]");
                    //todo 生产者发送消息 属性可以自由配置
                    MessageProperties messageProperties = new MessageProperties();
                    rabbitTemplate.send("topic-exchange",
                            routeKey,
                            new Message(str.getBytes(), messageProperties));
                }
            }
            opt = "suc";
        } catch (Exception e) {
            opt = e.getCause().toString();
        }
        return opt;
    }

}
