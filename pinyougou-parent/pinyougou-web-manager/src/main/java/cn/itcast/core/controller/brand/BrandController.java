package cn.itcast.core.controller.brand;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.service.brand.BrandService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {
    @Reference
    private BrandService brandService;// 拿到的引用是实现类的代理对象,

    @RequestMapping("/findAll.do")
    public List<Brand> findAll() {
        return brandService.findAll();
    }

    /**
     * 无条件的分页查询
     */
    @RequestMapping("/findPage.do")
    public PageResult findPage(Integer pageNo, Integer pageSize) {
        return brandService.findPage(pageNo, pageSize);
    }

    /**
     * 有条件的分页查询
     */

    @RequestMapping("/search.do")
    public PageResult search(Integer pageNo, Integer pageSize, @RequestBody Brand brand) {
        return brandService.search(pageNo, pageSize, brand);
    }

    /**
     * 添加品牌
     *
     * @param brand
     * @return
     */
    @RequestMapping("/add.do")
    public Result add(@RequestBody Brand brand) {
        try {
            brandService.add(brand);
            return new Result(true, "保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "保存失败");
        }
    }

    /**
     * 品牌更新数据回显
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne.do")
    public Brand findOne(Long id) {
        return brandService.findOne(id);
    }

    /**
     * 更新品牌信息
     *
     * @param brand
     * @return
     */
    @RequestMapping("/update.do")
    public Result update(@RequestBody Brand brand) {
        try {
            brandService.update(brand);
            return new Result(true, "更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "更新失败");
        }
    }

    @RequestMapping("/delete.do")
    public Result delete(Long[] ids) {
        if (ids != null && ids.length > 0) {
            try {
                brandService.delete(ids);
                return new Result(true, "删除成功");
            } catch (Exception e) {
                e.printStackTrace();
                return new Result(false, "删除失败");
            }
        } else {
            return new Result(false, "请选择要删除的数据");
        }
    }

    @RequestMapping("/selectOptionList.do")
    public List<Map<String,String>> selectOptionList(){
        return brandService.selectOptionList();
    }
}
