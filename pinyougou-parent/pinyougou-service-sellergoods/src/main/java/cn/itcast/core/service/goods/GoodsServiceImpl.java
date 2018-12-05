package cn.itcast.core.service.goods;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.good.GoodsQuery;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.service.staticpage.StaticPageService;
import cn.itcast.core.vo.GoodsVo;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.jms.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GoodsServiceImpl implements GoodsService {
    @Resource
    private GoodsDao goodsDao;
    @Resource
    private GoodsDescDao goodsDescDao;
    @Resource
    private ItemDao itemDao;
    @Resource
    private ItemCatDao itemCatDao;
    @Resource
    private BrandDao brandDao;
    @Resource
    private SellerDao sellerDao;
    @Resource
    private SolrTemplate solrTemplate;

    @Resource
    private JmsTemplate jmsTemplate;
    @Resource
    private Destination topicPageAndSolrDestination;
    @Resource
    private Destination queueSolrDeleteDestination;

    /**
     * 商家保存商品信息
     *
     * @param goodsVo
     */
    @Transactional
    @Override
    public void add(GoodsVo goodsVo) {
        //获取商品基本信息
        Goods goods = goodsVo.getGoods();

        //获取商品详情信息
        GoodsDesc goodsDesc = goodsVo.getGoodsDesc();

        //获取商品库存信息
        List<Item> itemList = goodsVo.getItemList();

        //保存商品基本信息并返回自增主键
        goods.setAuditStatus("0");
        goodsDao.insertSelective(goods);

        //保存商品详情信息
        goodsDesc.setGoodsId(goods.getId()); //设置外键
        goodsDescDao.insertSelective(goodsDesc);

        //保存商品库存信息(SKU:Item)
        //判断是否启用规则
        if ("1".equals(goods.getIsEnableSpec())) {//如果启用规格则为一对多
            if (itemList != null && itemList.size() > 0) {
                for (Item item : itemList) {
                    //获取标题:spu名称+spu的副标题+规格
                    String title = goods.getGoodsName() + " " + goods.getCaption();

                    //获取规格信息
                    Map<String, String> map = JSON.parseObject(item.getSpec(), Map.class);
                    Set<Map.Entry<String, String>> entrySet = map.entrySet();
                    for (Map.Entry<String, String> entry : entrySet) {
                        title += " " + entry.getValue();
                    }
                    item.setTitle(title);
                    setAttributeForItem(goods, goodsDesc, item);
                    itemDao.insertSelective(item);
                }
            } else { //不启用规则为一对一
                Item item = new Item();

                //设置title
                String title = goods.getGoodsName() + " " + goods.getCaption();
                item.setTitle(title);

                //商品价格
                item.setPrice(goods.getPrice());

                //库存量
                item.setNum(9999);

                //是否默认
                item.setIsDefault("1");

                //设置规格
                item.setSpec("{}");
                setAttributeForItem(goods, goodsDesc, item);

                //添加商品库存
                itemDao.insertSelective(item);
            }
        }

    }

    /**
     * 商家商品管理列表分页查询
     *
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, Goods goods) {
        //设置分页条件
        PageHelper.startPage(page, rows);

        //设置查询条件
        GoodsQuery goodsQuery = new GoodsQuery();
        GoodsQuery.Criteria criteria = goodsQuery.createCriteria();
        if (goods.getGoodsName() != null && !"".equals(goods.getGoodsName().trim())) {
            criteria.andGoodsNameLike("%" + goods.getGoodsName().trim() + "%");
        }
        if (goods.getAuditStatus() != null && !"".equals(goods.getAuditStatus().trim())) {
            criteria.andAuditStatusEqualTo(goods.getAuditStatus().trim());
        }
        goodsQuery.setOrderByClause("id desc");

        //根据条件查询
        Page<Goods> p = (Page<Goods>) goodsDao.selectByExample(goodsQuery);

        //封装查询结果并返回
        return new PageResult(p.getTotal(), p.getResult());
    }

    /**
     * 商家商品回显
     *
     * @param id
     * @return
     */
    @Override
    public GoodsVo findOne(Long id) {
        GoodsVo goodsVo = new GoodsVo();
        Goods goods = goodsDao.selectByPrimaryKey(id);
        goodsVo.setGoods(goods);

        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
        goodsVo.setGoodsDesc(goodsDesc);

        //库存信息
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(id);
        List<Item> items = itemDao.selectByExample(itemQuery);
        goodsVo.setItemList(items);

        return goodsVo;

    }

    /**
     * 商家更新商品
     *
     * @param goodsVo
     */
    @Transactional
    @Override
    public void update(GoodsVo goodsVo) {
        Goods goods = goodsVo.getGoods();
        goodsDao.updateByPrimaryKeySelective(goods);

        GoodsDesc goodsDesc = goodsVo.getGoodsDesc();
        goodsDescDao.updateByPrimaryKeySelective(goodsDesc);

        //先删除商品库存信息(条件删除)
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(goods.getId());
        itemDao.deleteByExample(itemQuery);

        //更新商品
        //判断是否启用规则
        if ("1".equals(goods.getIsEnableSpec())) {//如果启用规格则为一对多
            //获取商品库存信息
            List<Item> itemList = goodsVo.getItemList();
            if (itemList != null && itemList.size() > 0) {
                for (Item item : itemList) {
                    //获取标题:spu名称+spu的副标题+规格
                    String title = goods.getGoodsName() + " " + goods.getCaption();

                    //获取规格信息
                    Map<String, String> map = JSON.parseObject(item.getSpec(), Map.class);
                    Set<Map.Entry<String, String>> entrySet = map.entrySet();
                    for (Map.Entry<String, String> entry : entrySet) {
                        title += " " + entry.getValue();
                    }
                    item.setTitle(title);
                    setAttributeForItem(goods, goodsDesc, item);
                    itemDao.insertSelective(item);
                }
            } else { //不启用规则为一对一
                Item item = new Item();

                //设置title
                String title = goods.getGoodsName() + " " + goods.getCaption();
                item.setTitle(title);

                //商品价格
                item.setPrice(goods.getPrice());

                //库存量
                item.setNum(9999);

                //是否默认
                item.setIsDefault("1");

                //设置规格
                item.setSpec("{}");
                setAttributeForItem(goods, goodsDesc, item);

                //添加商品库存
                itemDao.insertSelective(item);
            }
        }

    }

    /**
     * 运营商管理商家未审商品核列表分页查询
     *
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    @Override
    public PageResult searchByManager(Integer page, Integer rows, Goods goods) {
        //设置查询条件
        PageHelper.startPage(page, rows);

        //设置查询条件
        GoodsQuery goodsQuery = new GoodsQuery();
        GoodsQuery.Criteria criteria = goodsQuery.createCriteria();
        if (goods.getGoodsName() != null && !"".equals(goods.getGoodsName().trim())) {
            criteria.andGoodsNameLike("%" + goods.getGoodsName().trim() + "%");
        }
        if (goods.getAuditStatus() != null && !"".equals(goods.getAuditStatus().trim())) {
            criteria.andAuditStatusEqualTo(goods.getAuditStatus().trim());
        }
        criteria.andIsDeleteIsNull();

        goodsQuery.setOrderByClause("id desc");
        //查询商品
        Page<Goods> p = (Page<Goods>) goodsDao.selectByExample(goodsQuery);

        return new PageResult(p.getTotal(), p.getResult());
    }

    /**
     * 运营商商品审核
     *
     * @param ids
     * @param status
     */
    @Override
    public void updateStatus(Long[] ids, String status) {
        if (ids != null && ids.length > 0) {
            Goods goods = new Goods();
            goods.setAuditStatus(status);
            for (final Long id : ids) {
                goods.setId(id);
                goodsDao.updateByPrimaryKeySelective(goods);
                if ("1".equals(status)) { // 审核成功
                    // 将商品保存到索引库
                   // updateSolr(id);
                    // 生成商品详情的静态页
                   // staticPageService.getHtml(id);

                    //使用activeMQ发送消息队列分别执行   将商品保存到索引库  和  生成商品详情静态页
                    jmsTemplate.send(topicPageAndSolrDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            //将消息封装成消息体进行发送
                            TextMessage textMessage = session.createTextMessage(String.valueOf(id));

                            return textMessage;
                        }
                    });
                }
            }
        }
    }

    /**
     * 运营商删除商品
     *
     * @param ids
     */
    @Transactional
    @Override
    public void delete(Long[] ids) {
        if (ids != null && ids.length > 0) {
            Goods goods = new Goods();
            goods.setIsDelete("1");
            for (final Long id : ids) {
                goods.setId(id);
                goodsDao.updateByPrimaryKeySelective(goods);
                // 删除索引库
               /* SimpleQuery query = new SimpleQuery("item_goodsid:" + id);
                solrTemplate.delete(query);
                solrTemplate.commit();*/
               //通过activeMQ发送消息,在service-search服务中删除索引库
                jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        //创建文本消息
                        TextMessage textMessage = session.createTextMessage(String.valueOf(id));
                        return textMessage;
                    }
                });
            }
        }
    }

    /**
     * 商品审核成功后添加索引库(上架)
     *
     * @param id
     */
    private void updateSolr(Long id) {
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
     * 将所有商品添加到索引库
     */
    private void dataImportToSolr() {
        //查询所有可用库存
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andStatusEqualTo("1");

        List<Item> itemList = itemDao.selectByExample(itemQuery);

        //处理动态字段
        if (itemList != null && itemList.size() > 0) {
            for (Item item : itemList) {
                String spec = item.getSpec();
                Map map = JSON.parseObject(spec, Map.class);
                item.setSpecMap(map);
            }
        }

        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
    }

    private void setAttributeForItem(Goods goods, GoodsDesc goodsDesc, Item item) {
        //图片信息
        List<Map> maps = JSON.parseArray(goodsDesc.getItemImages(), Map.class);
        if (maps != null && maps.size() > 0) {
            String url = maps.get(0).get("url").toString();
            item.setImage(url);
        }

        //三级分类
        item.setCategoryid(goods.getCategory3Id());

        //产品状态
        item.setStatus("1");

        //创建和更新的时间
        item.setCreateTime(new Date());
        item.setUpdateTime(new Date());

        //商品id
        item.setGoodsId(goods.getId());

        //商家id
        item.setSellerId(goods.getSellerId());

        //三级分类名称
        item.setCategory(itemCatDao.selectByPrimaryKey(goods.getCategory3Id()).getName());

        //品牌名称
        item.setBrand(brandDao.selectByPrimaryKey(goods.getBrandId()).getName());

        //商家名称
        item.setSeller(sellerDao.selectByPrimaryKey(goods.getSellerId()).getNickName());
    }

}
