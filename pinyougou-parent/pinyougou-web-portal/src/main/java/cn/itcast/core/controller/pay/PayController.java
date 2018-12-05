package cn.itcast.core.controller.pay;

import cn.itcast.core.entity.Result;
import cn.itcast.core.service.pay.PayService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {


    @Reference
    private PayService payService;

    /**
     * 支付页面需要的数据：二维码
     * @return
     */
    @RequestMapping("/createNative.do")
    public Map<String, String> createNative() throws Exception{
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return payService.createNative(username);
    }


    // 交易状态trade_state
    // 成功：返回操作的结果集
    // 失败：跳转到失败页面
    // 二维码生成：但是没有付钱（过期时间：30分钟）：等待：30  二微码超时
    /**
     * 查询订单
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus.do")
    public Result queryPayStatus(String out_trade_no){
        int time = 0;
        while (true){
            try {
                Map<String, String> map = payService.queryPayStatus(out_trade_no);
                String trade_state = map.get("trade_state");
                if("SUCCESS".equals(trade_state)){ // 成功
                    return new Result(true, "支付成功");
                }else{
                    // 等待支付
                    Thread.sleep(5000);
                    time++;
                }
                // 超过半个小时，二维码失效
                if(time > 360){
                    return new Result(false, "二维码超时");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
