package cn.itcast.core.service.order;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.dao.order.OrderItemDao;
import cn.itcast.core.pojo.cart.Cart;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.uinquekey.IdWorker;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderDao orderDao;

    @Resource
    private OrderItemDao orderItemDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private IdWorker idWorker;

    @Resource
    private ItemDao itemDao;

    @Resource
    private PayLogDao payLogDao;


    /**
     * 提交订单操作
     * @param username
     * @param order
     */
    @Transactional
    @Override
    public void add(String username, Order order) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("BUYER_CART").get(username);
        if(cartList != null && cartList.size() > 0){
            double fee = 0f;
            List<Long> orderList = new ArrayList<>();
            for (Cart cart : cartList) {
                // 1、保存订单：以商家为单位
                long orderId = idWorker.nextId();
                orderList.add(orderId);
                order.setOrderId(orderId);  // 主键
                double payment = 0f;        // 该商家下订单的总金额
                order.setPaymentType("1");  // 支付方式：在线支付
                order.setStatus("1");       // 订单状态：未付款
                order.setCreateTime(new Date());    // 订单创建日期
                order.setUpdateTime(new Date());    // 订单更新日期
                order.setSourceType("2");           // 订单的来源：pc端
                order.setSellerId(cart.getSellerId());  // 商家id
                // 2、保存订单明细
                List<OrderItem> orderItemList = cart.getOrderItemList();
                if(orderItemList != null && orderItemList.size() > 0){
                    for (OrderItem orderItem : orderItemList) {
                        long id = idWorker.nextId();
                        orderItem.setId(id);            // 主键
                        orderItem.setOrderId(orderId);  // 外键
                        Item item = itemDao.selectByPrimaryKey(orderItem.getItemId());
                        orderItem.setGoodsId(item.getGoodsId()); // 商品id
                        orderItem.setTitle(item.getTitle());     // 商品标题
                        orderItem.setPrice(item.getPrice());     // 单价
                        orderItem.setPicPath(item.getImage());   // 图片
                        orderItem.setSellerId(item.getSellerId()); // 商家id
                        double totalFee = item.getPrice().doubleValue() * orderItem.getNum();
                        orderItem.setTotalFee(new BigDecimal(totalFee));          // 订单明细的总价格
                        payment += totalFee;    // 该商家下的订单总金额
                        orderItemDao.insertSelective(orderItem);
                    }
                }
                order.setPayment(new BigDecimal(payment));
                orderDao.insertSelective(order);
                fee += payment; // 本地订单的总金额
            }

            // 生成交易日志
            PayLog payLog = new PayLog();
            payLog.setOutTradeNo(String.valueOf(idWorker.nextId()));    // 交易流水号
            payLog.setCreateTime(new Date());       // 生成交易日志的时间
            payLog.setTotalFee((long) fee*100);     // 支付总金额
            payLog.setUserId(username);             // 用户
            payLog.setTradeState("0");              // 支付状态：0，未支付
            payLog.setOrderList(orderList.toString().replace("[", "").replace("]", ""));  // list"[1,2,3...]"
            payLog.setPayType("1");                 // 支付类型：在线支付
            payLogDao.insertSelective(payLog);
            // 生成的交易日志：同步到缓存中一份
            redisTemplate.boundHashOps("paylog").put(username, payLog);

        }
        // 3、清空购物车
        redisTemplate.boundHashOps("BUYER_CART").delete(username);
    }
}
