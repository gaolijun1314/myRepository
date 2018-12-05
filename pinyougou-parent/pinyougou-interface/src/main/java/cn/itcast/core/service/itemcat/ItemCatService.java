package cn.itcast.core.service.itemcat;

import cn.itcast.core.pojo.item.ItemCat;

import java.util.List;


public interface ItemCatService {

    /**
     * 商品分类列表查询
     */
    public List<ItemCat> findByParentId(Long parentId);

    /**
     * 查询分类表中的模板id
     * @param id
     * @return
     */
    public ItemCat findOne(Long id);

    /**
     * 查询所有分类,在商家商品管理页面显示一级,二级,三级分类
     * @return
     */
    public List<ItemCat> findAll();
}
