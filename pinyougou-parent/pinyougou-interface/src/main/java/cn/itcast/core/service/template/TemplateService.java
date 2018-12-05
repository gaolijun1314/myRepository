package cn.itcast.core.service.template;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.template.TypeTemplate;

import java.util.List;
import java.util.Map;

public interface TemplateService {
    /**
     * 分页查询所有模板
     * @param page
     * @param rows
     * @param typeTemplate
     * @return
     */
    public PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate);

    /**
     * 新建模板
     * @param typeTemplate
     */
    public void add(TypeTemplate typeTemplate);

    /**
     * 批量删除模板
     * @param ids
     */
    public void delete(Long[] ids);

    /**
     * 更新模板数据回显
     * @param id
     * @return
     */
    public TypeTemplate findOne(Long id);

    /**
     * 更新模板数据
     * @param typeTemplate
     */
    public void update(TypeTemplate typeTemplate);

    /**
     * 根据模板id获取该模板下的规格和规格项的结果集
     * @param id
     * @return
     */
    public List<Map> findBySpecList(Long id);
}
