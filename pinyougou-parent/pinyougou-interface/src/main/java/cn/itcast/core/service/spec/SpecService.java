package cn.itcast.core.service.spec;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.vo.SpecVo;

import java.util.List;
import java.util.Map;

public interface SpecService {
    /**
     * 有条件的分页查询
     * @param page
     * @param rows
     * @param specification
     * @return
     */
    public PageResult search(Integer page, Integer rows, Specification specification);

    /**
     * 批量添加规格
     * @param specVo
     */
    public void add(SpecVo specVo);

    /**
     * 规格更新之数据回显
     * @param id
     * @return
     */
    public SpecVo findOne(Long id);

    /**
     * 规格更新
     * @param specVo
     */
    public void update(SpecVo specVo);

    /**
     * 批量删除规格
     * @param ids
     */
    public void delete(Long[] ids);

    /**
     * 商品类型模板  关联规格  数据回显
     * @return
     */
    public List<Map<String,String>> selectOptionList();

}
