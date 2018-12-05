package cn.itcast.core.service.brand;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl implements BrandService {
    @Resource
    BrandDao brandDao;

    @Override
    public List<Brand> findAll() {
        return brandDao.selectByExample(null);
    }

    /**
     * 无条件的分页查询
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findPage(Integer pageNo, Integer pageSize) {
        //使用分页助手设置分页条件
        PageHelper.startPage(pageNo, pageSize);

        //查询获取分页数据
        Page<Brand> page = (Page<Brand>) brandDao.selectByExample(null);

        //将总条数和结果集封装到pageResult中并返回
        PageResult pageResult = new PageResult(page.getTotal(), page.getResult());
        return pageResult;
    }

    /**
     * 有条件的分页查询
     *
     * @param pageNo
     * @param pageSize
     * @param brand
     * @return
     */
    @Override
    public PageResult search(Integer pageNo, Integer pageSize, Brand brand) {
        //使用分页助手设置分页条件
        PageHelper.startPage(pageNo, pageSize);

        //设置查询条件
        BrandQuery brandQuery = new BrandQuery();
        BrandQuery.Criteria criteria = brandQuery.createCriteria();
        //非空判断
        if (brand.getName() != null && !"".equals(brand.getName().trim())) {
            //拼接sql条件
            criteria.andNameLike("%" + brand.getName().trim() + "%");
        }
        if (brand.getFirstChar() != null && !"".equals(brand.getFirstChar().trim())) {
            //拼接sql条件
            criteria.andFirstCharEqualTo(brand.getFirstChar());
        }

        //添加按照id降序排列的条件
        brandQuery.setOrderByClause("id desc");

        //查询获取分页数据
        Page<Brand> page = (Page<Brand>) brandDao.selectByExample(brandQuery);

        //将总条数和结果集封装到pageResult中并返回
        PageResult pageResult = new PageResult(page.getTotal(), page.getResult());
        return pageResult;
    }

    /**
     * 添加品牌
     *
     * @param brand
     */
    @Transactional
    @Override
    public void add(Brand brand) {
        brandDao.insertSelective(brand);
    }

    /**
     * 品牌更新数据回显
     *
     * @param id
     * @return
     */
    @Override
    public Brand findOne(Long id) {
        return brandDao.selectByPrimaryKey(id);
    }

    /**
     * 更新品牌信息
     *
     * @param brand
     */
    @Transactional
    @Override
    public void update(Brand brand) {
        brandDao.updateByPrimaryKeySelective(brand);
    }

    /**
     * 批量删除品牌
     * @param ids
     */
    @Transactional
    @Override
    public void delete(Long[] ids) {
        if (ids != null && ids.length > 0) {
            /*for (Long id : ids) {
                brandDao.deleteByPrimaryKey(id);  //定义批量删除的方法,让一条sql删除全部内容,不要一条一条的删除
            }*/
            brandDao.deleteByPrimaryKeys(ids);
        }
    }

    /**
     * 商品类型模板  关联品牌  数据回显
     * @return
     */
    @Override
    public List<Map<String, String>> selectOptionList() {
        return brandDao.selectOptionList();
    }
}
