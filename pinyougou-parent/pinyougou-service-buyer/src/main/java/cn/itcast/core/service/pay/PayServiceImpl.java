package cn.itcast.core.service.pay;

import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.http.HttpClient;
import cn.itcast.core.pojo.log.PayLog;

import cn.itcast.core.uinquekey.IdWorker;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PayServiceImpl implements PayService {

    @Resource
    private IdWorker idWorker;

    @Value("${appid}")
    private String appid;           // 微信公众账号或开放平台APP的唯一标识
    @Value("${partner}")
    private String partner;         // 财付通平台的商户账号
    @Value("${partnerkey}")
    private String partnerkey;      // 财付通平台的商户密钥
    @Value("${notifyurl}")
    private String notifyurl;       // 回调地址

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private PayLogDao payLogDao;

    /**
     * 支付页面需要的数据：二维码 httpclient：模拟浏览器发送请求
     * @return
     */
    @Override
    public Map<String, String> createNative(String username) throws Exception{
        Map<String, String> data = new HashMap<>();
        PayLog payLog = (PayLog) redisTemplate.boundHashOps("paylog").get(username);
        long out_trade_no = idWorker.nextId();
        // 1、微信统一下单的接口地址
        String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        // 2、组装请求参数：xml格式数据
        // 数据的格式："<xml><xxx> </xxx></xml>"
        // 可以将数据封装到map中，然后通过工具类将map转成xml
//        公众账号ID	appid	是	String(32)	wxd678efh567hg6787	微信支付分配的公众账号ID（企业号corpid即为此appId）
        data.put("appid", appid);
//        商户号	mch_id	是	String(32)	1230000109	微信支付分配的商户号
        data.put("mch_id", partner);
//        随机字符串	nonce_str	是	String(32)	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	随机字符串，长度要求在32位以内。推荐随机数生成算法
        data.put("nonce_str", WXPayUtil.generateNonceStr());
//        签名	sign	是	String(32)	C380BEC2BFD727A4B6845133519F3AD6	通过签名算法计算得出的签名值，详见签名生成算法
//        商品描述	body	是	String(128)	腾讯充值中心-QQ会员充值
        data.put("body", "品优购-订单支付");
//        商品简单描述，该字段请按照规范传递，具体请见参数规定
//        商户订单号	out_trade_no	是	String(32)	20150806125346	商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|* 且在同一个商户号下唯一。详见商户订单号
//        data.put("out_trade_no", String.valueOf(out_trade_no));
        data.put("out_trade_no", payLog.getOutTradeNo());
//        标价金额	total_fee	是	Int	88	订单总金额，单位为分，详见支付金额
//        data.put("total_fee", String.valueOf(payLog.getTotalFee())); // 支付金额
        data.put("total_fee", "1"); // 支付金额-测试
//        终端IP	spbill_create_ip	是	String(16)	123.12.12.123	APP和网页支付提交用户端ip，Native支付填调用微信支付API的机器IP。
        data.put("spbill_create_ip", "123.12.12.123");
//        通知地址	notify_url	是	String(256)	http://www.weixin.qq.com/wxpay/pay.php	异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
        data.put("notify_url", notifyurl);
//        交易类型	trade_type	是	String(16)	JSAPI
        data.put("trade_type", "NATIVE");
        String xmlParam = WXPayUtil.generateSignedXml(data, partnerkey);

        // 3、发送请求并且获取响应的结果
        HttpClient httpClient = new HttpClient(url);
        httpClient.setHttps(true);  // 设置https请求
        httpClient.setXmlParam(xmlParam);   // 请求的参数
        httpClient.post();

        // 4、响应结果
        String strXML = httpClient.getContent();// 获取响应结果
        Map<String, String> map = WXPayUtil.xmlToMap(strXML);
        // 支付订单号
        map.put("out_trade_no", payLog.getOutTradeNo());
        // 支付金额-调微信接口（金额单位：分）
        map.put("total_fee", String.valueOf(payLog.getTotalFee())); // 在页面中显示的金额

        return map;
    }

    /**
     * 查询订单api
     * @param out_trade_no
     * @return
     */
    @Override
    public Map<String, String> queryPayStatus(String out_trade_no) throws Exception {
        // 1、封装请求参数
        Map<String, String> data = new HashMap<>();
        // 微信查询订单的api接口
        String url = "https://api.mch.weixin.qq.com/pay/orderquery";
//        公众账号ID	appid	是	String(32)	wxd678efh567hg6787	微信支付分配的公众账号ID（企业号corpid即为此appId）
        data.put("appid", appid);
//        商户号	mch_id	是	String(32)	1230000109	微信支付分配的商户号
        data.put("mch_id", partner);
//        商户订单号	out_trade_no	String(32)	20150806125346	商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*@ ，且在同一个商户号下唯一。 详见商户订单号
        data.put("out_trade_no", out_trade_no);
//        随机字符串	nonce_str	是	String(32)	C380BEC2BFD727A4B6845133519F3AD6	随机字符串，不长于32位。推荐随机数生成算法
        data.put("nonce_str", WXPayUtil.generateNonceStr());
//        签名	sign	是
        String xmlParam = WXPayUtil.generateSignedXml(data, partnerkey);

        // 2、发送请求
        HttpClient httpClient = new HttpClient(url);
        httpClient.setHttps(true);
        httpClient.setXmlParam(xmlParam);
        httpClient.post();

        // 3、响应结果
        String strXML = httpClient.getContent();
        Map<String, String> map = WXPayUtil.xmlToMap(strXML);

        // 4、如果支付成功：需要更新支付日志表
        if("SUCCESS".equals(map.get("trade_state"))){
            PayLog payLog = new PayLog();
            payLog.setOutTradeNo(out_trade_no); // 交易流水：主键
            payLog.setPayTime(new Date());      // 支付时间
            payLog.setTransactionId(map.get("transaction_id")); // 第三方的交易流水
            payLog.setTradeState("1");          // 支付状态：1，成功
            payLogDao.updateByPrimaryKeySelective(payLog);

            // TODO 请求缓存
        }
        return map;
    }
}
