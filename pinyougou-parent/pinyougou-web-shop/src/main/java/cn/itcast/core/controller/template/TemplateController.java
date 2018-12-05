package cn.itcast.core.controller.template;

import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.service.template.TemplateService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/typeTemplate")
public class TemplateController {
    @Reference
    private TemplateService templateService;

    /**
     * 根据模板id查询模板
     * @param id
     * @return
     */
    @RequestMapping("/findOne.do")
    public TypeTemplate findOne(Long id){
        return templateService.findOne(id);
    }

    /**
     * 根据模板id获取该模板下的规格和规格项的结果集
     * @param id
     * @return
     */
    @RequestMapping("/findBySpecList.do")
    public List<Map> findBySpecList(Long id){
        return templateService.findBySpecList(id);
    }
}
