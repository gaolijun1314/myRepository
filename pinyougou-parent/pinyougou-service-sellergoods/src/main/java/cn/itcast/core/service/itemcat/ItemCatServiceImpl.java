package cn.itcast.core.service.itemcat;

import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemCatQuery;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.List;
@Service
public class ItemCatServiceImpl implements ItemCatService {
    @Resource
    private ItemCatDao itemCatDao;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 商品分类列表查询
     * @param parentId
     * @return
     */
    @Override
    public List<ItemCat> findByParentId(Long parentId) {

        //查询所有分裂并添加到缓存,以提高门户页面检索效率
        List<ItemCat> itemCatList = itemCatDao.selectByExample(null);
        if (itemCatList != null && itemCatList.size() > 0) {
            for (ItemCat itemCat : itemCatList) {
                redisTemplate.boundHashOps("itemCat").put(itemCat.getName(),itemCat.getTypeId());

            }
        }

        //设置查询条件
        ItemCatQuery itemCatQuery = new ItemCatQuery();
        ItemCatQuery.Criteria criteria = itemCatQuery.createCriteria();
        criteria.andParentIdEqualTo(parentId);

        //根据条件查询并返回
        return itemCatDao.selectByExample(itemCatQuery);
    }

    /**
     * 查询分类表中的模板id
     * @param id
     * @return
     */
    @Override
    public ItemCat findOne(Long id) {
        return itemCatDao.selectByPrimaryKey(id);
    }

    /**
     * 查询所有分类,在商家商品管理页面显示一级,二级,三级分类
     * @return
     */
    @Override
    public List<ItemCat> findAll() {
        return itemCatDao.selectByExample(null);
    }
}
