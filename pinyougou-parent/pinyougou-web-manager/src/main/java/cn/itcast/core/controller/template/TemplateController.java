package cn.itcast.core.controller.template;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.service.template.TemplateService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/typeTemplate")
public class TemplateController {

    @Reference
    private TemplateService templateService;

    /**
     * 有条件的分页查询
     * @param page
     * @param rows
     * @param typeTemplate
     * @return
     */
    @RequestMapping("/search.do")
    public PageResult search(Integer page, Integer rows, @RequestBody TypeTemplate typeTemplate){
       return templateService.search(page,rows,typeTemplate);
    }

    /**
     * 新建模板
     * @param typeTemplate
     * @return
     */
    @RequestMapping("/add.do")
    public Result add(@RequestBody TypeTemplate typeTemplate){
        try {
            templateService.add(typeTemplate);
            return new Result(true,"新建成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"新建失败");
        }
    }

    /**
     * 批量删除模板
     * @param ids
     * @return
     */
    @RequestMapping("/delete.do")
    public Result delete(Long[] ids){
        try {
            templateService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }

    /**
     * 更新模板数据回显
     * @param id
     * @return
     */
    @RequestMapping("/findOne.do")
    public TypeTemplate findOne(Long id){
        return templateService.findOne(id);
    }

    /**
     * 更新模板
     * @param typeTemplate
     * @return
     */
    @RequestMapping("/update.do")
    public Result update(@RequestBody TypeTemplate typeTemplate){

        try {
            templateService.update(typeTemplate);
            return new Result(true,"更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"更新失败");
        }

    }
}
