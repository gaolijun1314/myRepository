package cn.itcast.core.controller.spec;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.service.spec.SpecService;
import cn.itcast.core.vo.SpecVo;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/specification")
public class SpecController {
    @Reference
    private SpecService specService;

    /**
     * 有条件的分页查询
     *
     * @param page
     * @param rows
     * @param specification
     * @return
     */
    @RequestMapping("/search.do")
    public PageResult search(Integer page, Integer rows, @RequestBody Specification specification) {

        return specService.search(page, rows, specification);
    }

    /**
     * 批量添加规格
     *
     * @param specVo
     * @return
     */
    @RequestMapping("/add.do")
    public Result add(@RequestBody SpecVo specVo) {
        try {
            specService.add(specVo);
            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }
    }

    /**
     * 规格修改之数据回显
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne.do")
    public SpecVo findOne(Long id) {
        return specService.findOne(id);
    }

    /**
     * 规格更新
     *
     * @param specVo
     * @return
     */
    @RequestMapping("/update.do")
    public Result update(@RequestBody SpecVo specVo) {
        try {
            specService.update(specVo);
            return new Result(true, "更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "更新失败");
        }
    }

    /**
     * 批量删除规格
     *
     * @param ids
     * @return
     */
    @RequestMapping("/delete.do")
    public Result delete(Long[] ids) {
        try {
            specService.delete(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }
    @RequestMapping("/selectOptionList.do")
    public List<Map<String, String>> selectOptionList(){
        return specService.selectOptionList();
    }
}
