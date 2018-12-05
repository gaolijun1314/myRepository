package cn.itcast.core.service.spec;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.specification.SpecificationQuery;
import cn.itcast.core.vo.SpecVo;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class SpecServiceImpl implements SpecService {
    @Resource
    private SpecificationDao specificationDao;
    @Resource
    private SpecificationOptionDao specificationOptionDao;

    /**
     * 根据条件分页查询
     *
     * @param page
     * @param rows
     * @param specification
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, Specification specification) {
        //设置分页条件
        PageHelper.startPage(page, rows);

        //设置查询条件
        SpecificationQuery specificationQuery = new SpecificationQuery();
        SpecificationQuery.Criteria criteria = specificationQuery.createCriteria();
        if (specification.getSpecName() != null && !"".equals(specification.getSpecName().trim())) {
            criteria.andSpecNameLike("%" + specification.getSpecName().trim() + "%");
        }

        //设置根据id降序排列
        specificationQuery.setOrderByClause("id desc");

        //根据条件分页查询
        Page<Specification> p = (Page<Specification>) specificationDao.selectByExample(specificationQuery);

        //创建PageResult对象并返回
        PageResult pageResult = new PageResult(p.getTotal(), p.getResult());
        return pageResult;

    }

    /**
     * 批量添加规格
     *
     * @param specVo
     */
    @Transactional
    @Override
    public void add(SpecVo specVo) {
        //获取到规格对象
        Specification specification = specVo.getSpecification();

        //获取规格项
        List<SpecificationOption> specificationOptionList = specVo.getSpecificationOptionList();

        //添加规格
        specificationDao.insertSelective(specification);//此处会返回自增主键

        if (specificationOptionList != null && specificationOptionList.size() > 0) {
            //添加规格项
            for (SpecificationOption specificationOption : specificationOptionList) {
                //设置自增主键
                specificationOption.setSpecId(specification.getId());
                //specificationOptionDao.insertSelective(specificationOption);  这种方式不好,需要批量添加
            }

            specificationOptionDao.insertSelectives(specificationOptionList);
        }
    }

    /**
     * 规格修改之数据回显
     *
     * @param id
     * @return
     */

    @Override
    public SpecVo findOne(Long id) {
        //查询规格
        Specification specification = specificationDao.selectByPrimaryKey(id);

        //条件查询规格选项
        SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
        specificationOptionQuery.createCriteria().andSpecIdEqualTo(id);
        List<SpecificationOption> specificationOptionList = specificationOptionDao.selectByExample(specificationOptionQuery);
        /*for (SpecificationOption specificationOption : specificationOptionList) {
            System.out.println(specificationOption.getOptionName());
        }*/

        //封装vo对象并返回
        return new SpecVo(specification, specificationOptionList);
    }

    /**
     * 规格更新
     *
     * @param specVo
     */
    @Transactional
    @Override
    public void update(SpecVo specVo) {
        //更新规格
        Specification specification = specVo.getSpecification();
        //System.out.println(specification.getId());
        specificationDao.updateByPrimaryKeySelective(specification);

        //先删除当前规格项(根据条件删除)
        SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
        specificationOptionQuery.createCriteria().andSpecIdEqualTo(specification.getId());
        specificationOptionDao.deleteByExample(specificationOptionQuery);

        //  List<SpecificationOption> specificationOptions = specificationOptionDao.selectByExample(specificationOptionQuery);

        //再更新规格项
        List<SpecificationOption> specificationOptionList = specVo.getSpecificationOptionList();

        if (specificationOptionList != null && specificationOptionList.size() > 0) {
            //设置外键
            for (SpecificationOption specificationOption : specificationOptionList) {
                specificationOption.setSpecId(specification.getId());
            }
        }

        //批量添加
        specificationOptionDao.insertSelectives(specificationOptionList);

    }

    /**
     * 批量删除规格
     * @param ids
     */

    @Override
    public void delete(Long[] ids) {
        if (ids != null && ids.length > 0) {
            for (Long id : ids) {
                //删除规格
                specificationDao.deleteByPrimaryKey(id);

                //根据外键条件删除规格项
                SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
                specificationOptionQuery.createCriteria().andSpecIdEqualTo(id);
                specificationOptionDao.deleteByExample(specificationOptionQuery);
            }
        }
    }

    /**
     * 商品类型模板  关联规格  数据回显
     * @return
     */
    @Override
    public List<Map<String, String>> selectOptionList() {
        return specificationDao.selectOptionList();
    }
}
