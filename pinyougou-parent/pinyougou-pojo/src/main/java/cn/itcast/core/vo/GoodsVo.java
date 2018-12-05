package cn.itcast.core.vo;

import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;

import java.io.Serializable;
import java.util.List;

/**
 * 封装商品的数据
 */
public class GoodsVo implements Serializable {
    private Goods goods;            //商品基本信息
    private GoodsDesc goodsDesc;    //商品描述信息
    private List<Item> itemList;    //商品对应的库存信息

    public Goods getGoods() {
        return goods;
    }

    public void setGoods(Goods goods) {
        this.goods = goods;
    }

    public GoodsDesc getGoodsDesc() {
        return goodsDesc;
    }

    public void setGoodsDesc(GoodsDesc goodsDesc) {
        this.goodsDesc = goodsDesc;
    }

    public List<Item> getItemList() {
        return itemList;
    }

    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
    }

    public GoodsVo() {

    }

    public GoodsVo(Goods goods, GoodsDesc goodsDesc, List<Item> itemList) {

        this.goods = goods;
        this.goodsDesc = goodsDesc;
        this.itemList = itemList;
    }
}
