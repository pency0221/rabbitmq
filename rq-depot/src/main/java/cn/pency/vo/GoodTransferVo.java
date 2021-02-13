package cn.pency.vo;

import java.io.Serializable;

/**

 *类说明：
 */
public class GoodTransferVo  implements Serializable {

    private String goodsId;
    private int changeAmount;
    private boolean inOrOut;

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public int getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(int changeAmount) {
        this.changeAmount = changeAmount;
    }

    public boolean isInOrOut() {
        return inOrOut;
    }

    public void setInOrOut(boolean inOrOut) {
        this.inOrOut = inOrOut;
    }
}
