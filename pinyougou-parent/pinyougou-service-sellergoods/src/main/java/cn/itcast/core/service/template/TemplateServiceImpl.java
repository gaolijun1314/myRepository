package cn.itcast.core.service.template;

import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.pojo.template.TypeTemplateQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class TemplateServiceImpl implements TemplateService {

    @Resource
    private TypeTemplateDao typeTemplateDao;

    @Resource
    private SpecificationOptionDao specificationOptionDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 有条件分页查询模板列表
     */
    @Override
    public PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate) {

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

        //设置分页条件
        PageHelper.startPage(page, rows);

        //设置查询条件
        TypeTemplateQuery typeTemplateQuery = new TypeTemplateQuery();
        if (typeTemplate.getName() != null && !"".equals(typeTemplate.getName().trim())) {
            typeTemplateQuery.createCriteria().andNameLike("%" + typeTemplate.getName().trim() + "%");
        }

        //按照id降序
        typeTemplateQuery.setOrderByClause("id desc");
        //根据条件查询
        Page<TypeTemplate> p = (Page<TypeTemplate>) typeTemplateDao.selectByExample(typeTemplateQuery);

        //封装结果并返回
        return new PageResult(p.getTotal(), p.getResult());
    }

    /**
     * 新建模板
     *
     * @param typeTemplate
     */
    @Transactional
    @Override
    public void add(TypeTemplate typeTemplate) {
        typeTemplateDao.insertSelective(typeTemplate);
    }

    /**
     * 批量删除模板
     *
     * @param ids
     */
    @Transactional
    @Override
    public void delete(Long[] ids) {
        if (ids != null && ids.length > 0) {
            /*for (Long id : ids) {
                typeTemplateDao.deleteByPrimaryKey(id);
            }*/
            //批量删除
            typeTemplateDao.deleteByPrimaryKeys(ids);
        }
    }

    /**
     * 更新模板数据回显
     *
     * @param id
     * @return
     */
    @Transactional
    @Override
    public TypeTemplate findOne(Long id) {
        return typeTemplateDao.selectByPrimaryKey(id);
    }

    /**
     * 更新模板
     *
     * @param typeTemplate
     */
    @Transactional
    @Override
    public void update(TypeTemplate typeTemplate) {
        typeTemplateDao.updateByPrimaryKeySelective(typeTemplate);
    }

    /**
     * 根据模板id获取该模板下的规格和规格项的结果集
     *
     * @param id
     * @return
     */
    @Override
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
