package cn.pency.rpc;

import cn.pency.service.DepotManager;
import cn.pency.vo.GoodTransferVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**

 *类说明：
 */
@Component
public class DepotServiceImpl implements DepotService {

    @Autowired
    private DepotManager depotManager;

    @Override
    public void changeDepot(GoodTransferVo goodTransferVo) {
        depotManager.operDepot(goodTransferVo);
    }
}
