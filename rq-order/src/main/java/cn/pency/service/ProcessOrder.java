package cn.pency.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 *类说明：这里可以选择是RPC调用还是MQ调用
 */
@Service
public class ProcessOrder {
    private Logger logger = LoggerFactory.getLogger(ProcessOrder.class);
    //TODO 调用方式
    @Autowired
    @Qualifier("mq")   //使用mq的方式   //todo @Qualifier注解与@Autowired配合使用 明确注入哪个实现类
    //@Qualifier("rpc")    //使用rpc的方式
    private IProDepot proDepot;

    public void processOrder(String goodsId,int amount){
        try {
            Thread.sleep(80);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("--------------------["+goodsId+"]订单入库完成，准备变动库存！");
        proDepot.processDepot(goodsId,amount);

    }

}
