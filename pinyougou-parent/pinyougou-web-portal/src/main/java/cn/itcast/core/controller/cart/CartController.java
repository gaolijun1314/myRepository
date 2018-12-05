package cn.itcast.core.controller.cart;

import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.cart.Cart;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.service.cart.CartService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {


    // 解决跨域请求：需要服务器端支持
    // （原因：CORS 头缺少 'Access-Control-Allow-Origin'）
    //            response.setHeader("Access-Control-Allow-Origin", "http://localhost:9003");
    //            response.setHeader("Access-Control-Allow-Credentials", "true"); // 是否携带cookie数据
    // allowCredentials = "true" 默认的
    //    @CrossOrigin(origins = {"http://localhost:9003"}, allowCredentials = "true")


    @Reference
    private CartService cartService;

    /**
     * 将商品加入购物车
     * @param itemId
     * @param num
     * @param response
     * @return
     */
    @RequestMapping("/addGoodsToCartList.do")
    @CrossOrigin(origins = {"http://localhost:9003"})
    public Result addGoodsToCartList(Long itemId, Integer num,
                                     HttpServletRequest request, HttpServletResponse response){
        try {
            // 将商品加入购物车
            // 1、定义一个空车
            List<Cart> cartList = null;
            // 2、判断本地（客户端）是否有车子
            Cookie[] cookies = request.getCookies();
            if(cookies != null && cookies.length > 0){
                for (Cookie cookie : cookies) {
                    // cookie：key-value（json串）
                    // 3、有：取出购物车并赋值给定义的空车
                    if("BUYER_CART".equals(cookie.getName())){
                        String text = cookie.getValue();
                        cartList = JSON.parseArray(text, Cart.class);
                        break; // 找到购物车，跳出循环
                    }
                }
            }
            // 4、没有：需要创建购物车
            if(cartList == null){
                // 说明是第一次将商品加入购物车
                cartList = new ArrayList<>();
            }
            // ======有车了，将商品装车
            // 将数据封装到对象中
            Cart cart = new Cart();
            Item item = cartService.findOne(itemId);
            cart.setSellerId(item.getSellerId()); // 商家id
            List<OrderItem> orderItemList = new ArrayList<>();
            OrderItem orderItem = new OrderItem(); // 新的购物项// 给该对象瘦身，不易保存全部数据，因为cookie大小有限
            orderItem.setItemId(itemId);
            orderItem.setNum(num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);

            // 5、将商品装车
            // 5-1、判断该商品是否属于同一个商家（判断商家id即可）
            int sellerIndexOf = cartList.indexOf(cart); // 不存在：-1   存在：具体的脚标值
            if(sellerIndexOf != -1){
                // 属于同一个商家
                List<OrderItem> oldOrderItemList= cartList.get(sellerIndexOf).getOrderItemList();
                // 继续判断是否属于同款商品（判断库存id即可）
                int itemIndexOf = oldOrderItemList.indexOf(orderItem);
                if(itemIndexOf != -1){
                    // 同款商品：合并数量
                    OrderItem oldOrderItem = oldOrderItemList.get(itemIndexOf);
                    oldOrderItem.setNum(oldOrderItem.getNum() + num);
                }else{
                    // 不是同款商品：将购物项加入购物项集中
                    oldOrderItemList.add(orderItem);
                }
            }else{
                // 不属于：直接将商品装车
                cartList.add(cart);
            }
            // 6、将购物车保存到客户端（cookie）
            Cookie cookie = new Cookie("BUYER_CART", JSON.toJSONString(cartList));
            cookie.setMaxAge(60*60);
            // http:localhost:8080/project1/
            // http:localhost:8080/project2/
            cookie.setPath("/"); // 设置cookie共享
            response.addCookie(cookie);
            return new Result(true, "添加成功");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false, "添加失败");
        }
    }


    @RequestMapping("/findCartList.do")
    public List<Cart> findCartList(HttpServletRequest request){
        // 判断用户是否登录
        // 未登录：从客户端中获取
        List<Cart> cartList = null;
        Cookie[] cookies = request.getCookies();
        if(cookies != null && cookies.length > 0){
            for (Cookie cookie : cookies) {
                if("BUYER_CART".equals(cookie.getName())){
                    String text = cookie.getValue();
                    cartList = JSON.parseArray(text, Cart.class);
                    break;
                }
            }
        }
        // TODO 已登录：从服务器端获取

        // 需要对购物车的数据进行填充
        if(cartList != null){
            cartList = cartService.setAttributeForCart(cartList);
        }

        return cartList;
    }
}
