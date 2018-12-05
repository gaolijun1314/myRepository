package cn.itcast.core.service.pay;

import java.util.Map;

public interface PayService {

    /**
     * 支付页面需要的数据：二维码
     * @return
     */
    public Map<String, String> createNative(String username) throws Exception;

    /**
     * 查询订单api
     * @param out_trade_no
     * @return
     */
    public Map<String, String> queryPayStatus(String out_trade_no) throws Exception;
}
