package cn.itcast.core.service.brand;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.good.Brand;

import java.util.List;
import java.util.Map;

public interface BrandService {

    /**
     * 查询所有品牌
     */
    public List<Brand> findAll();

    /**
     * 无条件的分页查询
     */
    public PageResult findPage(Integer pageNo, Integer pageSize);

    /**
     * 有条件的分页查询
     */
    public PageResult search(Integer pageNo, Integer pageSize,Brand brand);

    /**
     * 添加品牌
     * @param brand
     */
    public void add(Brand brand);

    /**
     * 品牌更新数据回显
     * @param id
     * @return
     */
    public Brand findOne(Long id);

    /**
     * 更新品牌信息
     * @param brand
     */
    public void update(Brand brand);

    /**
     * 批量删除品牌
     * @param ids
     */
    public void delete(Long[] ids);

    /**
     * 商品类型模板  关联品牌  数据回显
     * @return
     */
    public List<Map<String,String>> selectOptionList();
}
