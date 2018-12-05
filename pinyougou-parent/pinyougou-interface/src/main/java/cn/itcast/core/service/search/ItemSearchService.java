package cn.itcast.core.service.search;

import java.util.Map;

public interface ItemSearchService {
    /**
     * 前台搜索分页加关键字高亮显示
     * @param searchMap
     * @return
     */
    public Map<String,Object> search(Map<String,Object> searchMap);

    /**
     * 将商品对应的库存信息保存到索引库
     * @param id
     */
    public void insertItemToSolr(Long id);

    /**
     * 从索引库中删除商品库粗信息
     * @param id
     */
    public void deleteItemFromSolr(Long id);
}
