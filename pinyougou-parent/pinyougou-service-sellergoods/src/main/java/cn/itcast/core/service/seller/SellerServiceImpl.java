package cn.itcast.core.service.seller;

import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.pojo.seller.SellerQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class SellerServiceImpl implements SellerService {
    @Resource
    private SellerDao sellerDao;

    /**
     * 商家入驻
     *
     * @param seller
     */
    @Transactional
    @Override
    public void add(Seller seller) {
        seller.setStatus("0");

        String password = seller.getPassword();
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encodePassworld = bCryptPasswordEncoder.encode(password);
        seller.setPassword(encodePassworld);

        sellerDao.insertSelective(seller);
    }

    /**
     * 带审核商家列表分页查询
     *
     * @param page
     * @param rows
     * @param seller
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, Seller seller) {
        //设置分页条件
        PageHelper.startPage(page, rows);

        //设置查询条件
        SellerQuery sellerQuery = new SellerQuery();
        SellerQuery.Criteria criteria = sellerQuery.createCriteria();
        if (seller.getName() != null && !"".equals(seller.getName().trim())) {
            criteria.andNameLike("%" + seller.getName().trim() + "%");
        }
        if (seller.getNickName() != null && !"".equals(seller.getNickName().trim())) {
            criteria.andNickNameLike("%" + seller.getNickName().trim() + "%");
        }
        if (seller.getStatus() != null && !"".equals(seller.getStatus().trim())) {
            criteria.andStatusEqualTo(seller.getStatus().trim());
        }

        //根据条件查询
        Page<Seller> p = (Page<Seller>) sellerDao.selectByExample(sellerQuery);

        //封装数据并返回
        return new PageResult(p.getTotal(), p.getResult());
    }

    /**
     * 商家详情的数据回显
     * @param sellerId
     * @return
     */
    @Override
    public Seller findOne(String sellerId) {
        return sellerDao.selectByPrimaryKey(sellerId);
    }

    /**
     * 卖家信息审核
     * @param sellerId
     * @param status
     */
    @Override
    public void updateStatus(String sellerId, String status) {
        Seller seller = new Seller();
        seller.setSellerId(sellerId);
        seller.setStatus(status);

        sellerDao.updateByPrimaryKeySelective(seller);
    }
}
