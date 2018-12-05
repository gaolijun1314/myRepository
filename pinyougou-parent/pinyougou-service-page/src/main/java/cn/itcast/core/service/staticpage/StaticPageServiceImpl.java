package cn.itcast.core.service.staticpage;

import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemQuery;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticPageServiceImpl implements StaticPageService,ServletContextAware {

    private Configuration configuration;
    public void setFreeMarkerConfigurer(FreeMarkerConfigurer freeMarkerConfigurer) {
        this.configuration = freeMarkerConfigurer.getConfiguration();
    }
    @Resource
    private GoodsDao goodsDao;
    @Resource
    private GoodsDescDao goodsDescDao;
    @Resource
    private ItemCatDao itemCatDao;
    @Resource
    private ItemDao itemDao;

    private ServletContext servletContext;

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void getHtml(Long id) {

        try {
            //1.创建configuration并制定模板位置
            //2.通过configuration获取模板
            Template template = configuration.getTemplate("item.ftl");

            //3.获取业务数据
            Map<String,Object> dataModel = getDataModel(id);

            //4.生成静态页
            String pathName = "/" + id + ".html";
            String realPath = servletContext.getRealPath(pathName);
            File file = new File(realPath);
            Writer out = new OutputStreamWriter(new FileOutputStream(file),"utf-8");
            template.process(dataModel,out);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String,Object> getDataModel(Long id) {
        Map<String,Object> dataModel = new HashMap<>();
        //1.获取商品基本信息
        Goods goods = goodsDao.selectByPrimaryKey(id);
        dataModel.put("goods",goods);

        //2.获取商品详情信息
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
        dataModel.put("goodsDesc",goodsDesc);

        //3.获取分类信息
        ItemCat itemCat1 = itemCatDao.selectByPrimaryKey(goods.getCategory1Id());
        ItemCat itemCat2 = itemCatDao.selectByPrimaryKey(goods.getCategory2Id());
        ItemCat itemCat3 = itemCatDao.selectByPrimaryKey(goods.getCategory3Id());
        dataModel.put("itemCat1",itemCat1);
        dataModel.put("itemCat2",itemCat2);
        dataModel.put("itemCat3",itemCat3);

        //4.获取商品库存信息
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(id).andNumGreaterThan(0);
        List<Item> itemList = itemDao.selectByExample(itemQuery);
        dataModel.put("itemList",itemList);

        return dataModel;
    }


}
