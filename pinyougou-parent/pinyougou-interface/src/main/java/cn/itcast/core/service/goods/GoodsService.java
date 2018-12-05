package cn.itcast.core.service.goods;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.vo.GoodsVo;

public interface GoodsService {
    /**
     * 添加商品
     *
     * @param goodsVo
     */
    public void add(GoodsVo goodsVo);

    /**
     * 商家商品管理列表分页查询
     *
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    public PageResult search(Integer page, Integer rows, Goods goods);

    /**
     * 商家商品回显
     *
     * @param id
     * @return
     */
    public GoodsVo findOne(Long id);

    /**
     * 商品更新
     *
     * @param goodsVo
     */
    public void update(GoodsVo goodsVo);

    /**
     * 运营商管理商家未审商品核列表分页查询
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    public PageResult searchByManager(Integer page, Integer rows, Goods goods);

    /**
     * 运营商商品审核
     * @param ids
     * @param status
     */
    public void updateStatus(Long[] ids,String status);

    /**
     * 运营商删除商品
     * @param ids
     */
    public void delete(Long[] ids);
}
