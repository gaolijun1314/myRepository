package cn.itcast.core.controller.itemcat;

import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.service.itemcat.ItemCatService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/itemCat")
public class ItemCatController {

    @Reference
    private ItemCatService itemCatService;

    /**
     * 商家产品三级分类查询
     * @param parentId
     * @return
     */
    @RequestMapping("/findByParentId.do")
    public List<ItemCat> findByParentId(Long parentId){
       return itemCatService.findByParentId(parentId);
    }

    /**
     * 查询分类表中的模板id
     * @param id
     * @return
     */
    @RequestMapping("/findOne.do")
    public ItemCat findOne(Long id){
        return itemCatService.findOne(id);
    }

    /**
     * 查询所有分类,在商家商品管理页面显示一级,二级,三级分类
     * @return
     */
    @RequestMapping("/findAll.do")
    public List<ItemCat> findAll(){
        return itemCatService.findAll();
    }
}
