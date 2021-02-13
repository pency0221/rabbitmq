package cn.pency.service;

import cn.pency.rpc.DepotService;
import cn.pency.rpc.RpcProxy;
import cn.pency.vo.GoodTransferVo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;

/**
 *类说明：使用RPC远程过程调用调用库存系统(端口10002)
 */
@Service
@Qualifier("rpc")
public class RpcMode implements IProDepot {

    private static final String IP = "127.0.0.1";
    private static final int PORT = 10002;

    public void processDepot(String goodsId, int amount) {

        DepotService service = RpcProxy.getRmoteProxyObj(DepotService.class,
                new InetSocketAddress(IP,PORT));
        GoodTransferVo goodTransferVo = new GoodTransferVo();
        goodTransferVo.setGoodsId(goodsId);
        goodTransferVo.setChangeAmount(amount);
        goodTransferVo.setInOrOut(true);
        service.changeDepot(goodTransferVo);
    }
}
