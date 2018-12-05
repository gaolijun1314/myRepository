package cn.itcast.core.service.cart;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.cart.Cart;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.pojo.seller.Seller;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Resource
    private ItemDao itemDao;

    @Resource
    private SellerDao sellerDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Item findOne(Long id) {
        return itemDao.selectByPrimaryKey(id);
    }

    /**
     * 设置购物车需要回显的数据
     * @param cartList
     * @return
     */
    @Override
    public List<Cart> setAttributeForCart(List<Cart> cartList) {
        // 进行数据填充
        for (Cart cart : cartList) {
            Seller seller = sellerDao.selectByPrimaryKey(cart.getSellerId());
            cart.setSellerName(seller.getNickName());   // 商家店铺名称
            List<OrderItem> orderItemList = cart.getOrderItemList();
            for (OrderItem orderItem : orderItemList) {
                Item item = itemDao.selectByPrimaryKey(orderItem.getItemId());
                orderItem.setPicPath(item.getImage());  // 商品图片
                orderItem.setTitle(item.getTitle());    // 商品标题
                orderItem.setPrice(item.getPrice());    // 商品单价
                BigDecimal totalFee = new BigDecimal(item.getPrice().doubleValue() * orderItem.getNum());
                orderItem.setTotalFee(totalFee);        // 商品小计
            }
        }
        return cartList;
    }

    /**
     * 将本地的购物车合并到redis中
     * @param username
     * @param newCartList
     */
    @Override
    public void mergeCartList(String username, List<Cart> newCartList) {
        // 1、从redis中取出老车
        List<Cart> oldCartList = (List<Cart>) redisTemplate.boundHashOps("BUYER_CART").get(username);
        // 2、将新车合并到老车中
        oldCartList = mergeNewCartListToOldCartList(newCartList, oldCartList);
        // 3、将老车保存到redis中
        redisTemplate.boundHashOps("BUYER_CART").put(username, oldCartList);
    }

    /**
     * 从redis中获取购物车
     * @param username
     * @return
     */
    @Override
    public List<Cart> getCartListFromRedis(String username) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("BUYER_CART").get(username);
        return cartList;
    }

    // 将新车合并到老车中
    private List<Cart> mergeNewCartListToOldCartList(List<Cart> newCartList, List<Cart> oldCartList) {
        // 开始合并
        if(newCartList != null){
            if(oldCartList != null){
                // 新车、老车都不为空：开始合并
                // 将新车合并到老车中，遍历新车
                for (Cart newCart : newCartList) {
                    // 判断是否是同一个商家
                    int sellerIndexOf = oldCartList.indexOf(newCart);
                    if(sellerIndexOf != -1){
                        // 同一个商家
                        List<OrderItem> oldOrderItemList = oldCartList.get(sellerIndexOf).getOrderItemList();
                        List<OrderItem> newOrderItemList = newCart.getOrderItemList();
                        // 继续判断，判断是否属于同款商品
                        for (OrderItem newOrderItem : newOrderItemList) {
                            int itemIndexOf = oldOrderItemList.indexOf(newOrderItem);
                            if(itemIndexOf != -1){
                                // 同款商品：合并数量
                                OrderItem oldOrderItem = oldOrderItemList.get(itemIndexOf);
                                oldOrderItem.setNum(oldOrderItem.getNum() + newOrderItem.getNum());
                            }else{
                                // 不是同款商品，直接添加到购物项集中
                                oldOrderItemList.add(newOrderItem);
                            }
                        }
                    }else{
                        // 不同商家，直接装车
                        oldCartList.add(newCart);
                    }
                }
            }else {
                // 如果老车为空，直接返回新车
                return newCartList;
            }
        }else {
            // 如果新车为空，直接返回老车
            return oldCartList;
        }
        return oldCartList;
    }




}
