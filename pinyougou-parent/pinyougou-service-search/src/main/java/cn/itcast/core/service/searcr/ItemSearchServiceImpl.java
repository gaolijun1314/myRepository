package cn.itcast.core.service.searcr;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.service.search.ItemSearchService;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import javax.annotation.Resource;
import java.util.*;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {
    @Resource
    private SolrTemplate solrTemplate;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private ItemDao itemDao;

    @Override
    public Map<String, Object> search(Map<String, Object> searchMap) {

        //对关键字进行去空格处理
        String keywords = searchMap.get("keywords").toString();
        if (keywords != null && !"".equals(keywords)) {
            keywords.replace(" ","");
            searchMap.put("keywords",keywords);
        }

        //3.品牌列表  //4.规格和规格选项列表
        Map<String, Object> resultMap = new HashMap<>();
        // 1.根据关键字分页高亮查询商品列表
        Map<String, Object> goodsMap = searchForHighLightPage(searchMap);

        //2.分组查询分类名称列表
        List<String> categoryList = searchForGroupPage(searchMap);
        if (categoryList != null && categoryList.size() > 0) {
            //3.品牌列表  //4.规格和规格选项列表
            Map<String, Object> brandAndSpecMap = searchBrandAndSpecListByCategory(categoryList.get(0));

            resultMap.putAll(brandAndSpecMap);
            resultMap.put("categoryList", categoryList);
        }

        resultMap.putAll(goodsMap);
        return resultMap;

    }

    /**
     * 将商品对应的库存信息保存到索引库
     * @param id
     */
    @Override
    public void insertItemToSolr(Long id) {
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andStatusEqualTo("1").
                andIsDefaultEqualTo("1").andGoodsIdEqualTo(id);

        List<Item> items = itemDao.selectByExample(itemQuery);
        //处理动态字段
        if (items != null && items.size() > 0) {
            for (Item item : items) {
                String spec = item.getSpec();
                Map map = JSON.parseObject(spec, Map.class);
                item.setSpecMap(map);
            }
        }

        solrTemplate.saveBeans(items);
        solrTemplate.commit();
    }

    /**
     * 从索引库中删除商品库粗信息
     * @param id
     */
    @Override
    public void deleteItemFromSolr(Long id) {
        SimpleQuery query = new SimpleQuery("item_goodsid:"+id);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    //3.品牌列表  //4.规格和规格选项列表
    private Map<String, Object> searchBrandAndSpecListByCategory(String category) {
        Map<String, Object> map = new HashMap<>();

        //从缓存中获取第一个分类名称对应的模板id(需求为默认查询第一个分类下的模板)
        Object typeId = redisTemplate.boundHashOps("itemCat").get(category);

        //根据模板id从缓存中获取获取品牌结果集
        List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
        //根据模板id从缓存中获取获取规格及规格选项结果集
        List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
        map.put("brandList", brandList);
        map.put("specList", specList);
        return map;
    }

    //2.分组查询分类列表
    private List<String> searchForGroupPage(Map<String, Object> searchMap) {

        //设置检索条件
        Criteria criteria = new Criteria("item_keywords");
        String keywords = searchMap.get("keywords").toString();
        if (keywords != null && !"".equals(keywords)) {
            criteria.is(keywords);
        }
        SimpleQuery query = new SimpleQuery(criteria);
        //设置分组
        GroupOptions groupOptions = new GroupOptions();
        groupOptions.addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //查询分组结果
        GroupPage<Item> itemGroupPage = solrTemplate.queryForGroupPage(query, Item.class);

        //获取分组结果
        List<String> list = new ArrayList<>();
        GroupResult<Item> itemGroupResult = itemGroupPage.getGroupResult("item_category");
        Page<GroupEntry<Item>> groupEntries = itemGroupResult.getGroupEntries();
        for (GroupEntry<Item> groupEntry : groupEntries) {
            String groupValue = groupEntry.getGroupValue();
            // System.out.println("groupValue: " + groupValue);
            list.add(groupValue);
        }
        return list;
    }

    // 1.根据关键字分页高亮查询商品列表
    private Map<String, Object> searchForHighLightPage(Map<String, Object> searchMap) {

        Map<String, Object> goodsMap = new HashMap<>();
        //设置检索条件
        Criteria criteria = new Criteria("item_keywords");
        String keywords = searchMap.get("keywords").toString();
        if (keywords != null && !"".equals(keywords)) {
            criteria.is(keywords);
        }
        SimpleHighlightQuery query = new SimpleHighlightQuery(criteria);
        //设置分页查询条件
        Integer pageNo = Integer.parseInt(searchMap.get("pageNo").toString());
        Integer pageSize = Integer.parseInt(searchMap.get("pageSize").toString());
        query.setOffset((pageNo - 1) * pageSize);
        query.setRows(pageSize);
        //设置关键字高亮
        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");
        highlightOptions.setSimplePrefix("<font color='red'>");
        highlightOptions.setSimplePostfix("</font>");
        query.setHighlightOptions(highlightOptions);

        //设置分类过滤条件
        if (searchMap.get("category") != null && !"".equals(searchMap.get("category"))) {

            Criteria cri = new Criteria("item_category");
            cri.is(searchMap.get("category"));

            SimpleFilterQuery filterQuery = new SimpleFilterQuery(cri);
            query.addFilterQuery(filterQuery);
        }

        //设置品牌过滤条件
        if (searchMap.get("brand") != null && !"".equals(searchMap.get("brand"))) {

            Criteria cri = new Criteria("item_brand");
            cri.is(searchMap.get("brand"));

            SimpleFilterQuery filterQuery = new SimpleFilterQuery(cri);
            query.addFilterQuery(filterQuery);
        }

        //设置价格过滤
        if (searchMap.get("price") != null && !"".equals(searchMap.get("price"))) {
            String[] prices = searchMap.get("price").toString().split("-");
            Criteria cri = new Criteria("item_price");

            if (searchMap.get("price").toString().contains("*")) {  //价格在xxx以上
                cri.greaterThan(prices[0]);
            } else {
                cri.between(prices[0], prices[1], true, true);//价格在xxx和yyy之间
            }

            SimpleFilterQuery filterQuery = new SimpleFilterQuery(cri);
            query.addFilterQuery(filterQuery);
        }

        //设置规格过滤

        if (searchMap.get("spec") != null && !"".equals(searchMap.get("spec"))) {

            Map<String, String> spec = JSON.parseObject(searchMap.get("spec").toString(), Map.class);
            Set<Map.Entry<String, String>> entries = spec.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                Criteria cri = new Criteria("item_spec_" + entry.getKey());
                cri.is(entry.getValue());

                SimpleFilterQuery filterQuery = new SimpleFilterQuery(cri);
                query.addFilterQuery(filterQuery);
            }
        }

        // 根据价格以及新品排序：sortField：价格(排序字段)    sort：ASC(传递的值)
        if (searchMap.get("sort") != null && !"".equals(searchMap.get("sort"))) {

            if ("DESC".equals(searchMap.get("sort"))) {
                Sort sort = new Sort(Sort.Direction.DESC, "item_" + searchMap.get("sortField"));
                query.addSort(sort);
            } else {
                Sort sort = new Sort(Sort.Direction.ASC, "item_" + searchMap.get("sortField"));
                query.addSort(sort);
            }
        }

        //根据条件查询
        HighlightPage<Item> highlightPage = solrTemplate.queryForHighlightPage(query, Item.class);

        //封装高亮关键字highlights到普通结果集entity
        List<HighlightEntry<Item>> highlighted = highlightPage.getHighlighted();
        if (highlighted != null && highlighted.size() > 0) {
            //先取出高亮的字符串"<font color = 'red'>keyWords</font>"
            for (HighlightEntry<Item> itemHighlightEntry : highlighted) {
               /* String s = itemHighlightEntry.getHighlights().get(0).getSnipplets().get(0);
                itemHighlightEntry.getEntity().setTitle(s);*/

                List<HighlightEntry.Highlight> highlights = itemHighlightEntry.getHighlights();
                if (highlights != null && highlights.size() > 0) {
                    List<String> snipplets = highlights.get(0).getSnipplets();
                    if (snipplets != null && !"".equals(snipplets)) {
                        String s = snipplets.get(0);
                        itemHighlightEntry.getEntity().setTitle(s);
                    }
                }
            }
        }

        //将数据封装到结果集
        int totalPages = highlightPage.getTotalPages();
        long totalElements = highlightPage.getTotalElements();
        List<Item> content = highlightPage.getContent();
        goodsMap.put("totalPages", totalPages);
        goodsMap.put("total", totalElements);
        goodsMap.put("rows", content);
        return goodsMap;

    }


}
