package cn.itcast.core.service.cart;

import cn.itcast.core.pojo.cart.Cart;
import cn.itcast.core.pojo.item.Item;

import java.util.List;

public interface CartService {

    /**
     * 获取item对象
     * @param id
     * @return
     */
    public Item findOne(Long id);

    /**
     * 设置购物车需要回显的数据
     * @param cartList
     * @return
     */
    List<Cart> setAttributeForCart(List<Cart> cartList);

    /**
     * 将本地的购物车合并到redis中
     * @param username
     * @param cartList
     */
    void mergeCartList(String username, List<Cart> cartList);

    /**
     * 从redis中获取购物车
     * @param username
     * @return
     */
    List<Cart> getCartListFromRedis(String username);
}
