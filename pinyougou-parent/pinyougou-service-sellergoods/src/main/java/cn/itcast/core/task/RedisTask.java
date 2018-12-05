package cn.itcast.core.task;

import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
public class RedisTask {
    @Resource
    private ItemCatDao itemCatDao;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private TypeTemplateDao typeTemplateDao;
    @Resource
    private SpecificationOptionDao specificationOptionDao;

    /**
     * 将商品分类,模板id,模板中的品牌结果集和规格结果集放入缓存中的任务交给定时任务完成
     */
    @Scheduled(cron = "00 07 10 * * ?")
    public void autoDBToRedisForItemCat(){

        //查询所有分裂并添加到缓存,以提高门户页面检索效率
        List<ItemCat> itemCatList = itemCatDao.selectByExample(null);
        if (itemCatList != null && itemCatList.size() > 0) {
            for (ItemCat itemCat : itemCatList) {
                redisTemplate.boundHashOps("itemCat").put(itemCat.getName(),itemCat.getTypeId());
            }
        }
        System.out.println("定时缓存111执行了");
        //将模板中对应的品牌结果集和规格结果集放到缓存中
        List<TypeTemplate> typeTemplates = typeTemplateDao.selectByExample(null);
        if (typeTemplates != null && typeTemplates.size() > 0) {
            for (TypeTemplate template : typeTemplates) {
                //将模板中的品牌结果集放到缓存中
                List<Map> brandList = JSON.parseArray(template.getBrandIds(), Map.class);
                redisTemplate.boundHashOps("brandList").put(template.getId(),brandList);

                //将模板中的规格和规格选项结果集放到缓存中
                List<Map> specList = findBySpecList(template.getId());
                redisTemplate.boundHashOps("specList").put(template.getId(),specList);

            }
        }
        System.out.println("定时缓存222执行了");
    }

    /**
     * 根据模板id获取该模板下的规格和规格项的结果集
     *
     * @param id
     * @return
     */

    public List<Map> findBySpecList(Long id) {
        //根据id获取模板
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);

        //获取该模板下的规格结果集:[{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
        String specIds = typeTemplate.getSpecIds();

        //将返回的规格结果JSON字符串转换成对象
        List<Map> maps = JSON.parseArray(specIds, Map.class);

        //遍历得到每个map
        for (Map map : maps) {
            //获取该规格的id
            long specId = Long.parseLong(map.get("id").toString());

            //根据该规格id获取到对应的规格项并添加到map
            SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
            specificationOptionQuery.createCriteria().andSpecIdEqualTo(specId);
            List<SpecificationOption> options = specificationOptionDao.selectByExample(specificationOptionQuery);
            map.put("options", options);
        }
        return maps;
    }

}
