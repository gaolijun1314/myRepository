package cn.itcast.core.service.seller;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.seller.Seller;

public interface SellerService {
    /**
     * 商家入驻
     * @param seller
     */
    public void add(Seller seller);

    /**
     * 带审核商家列表分页查询
     * @param page
     * @param rows
     * @param seller
     * @return
     */
    public PageResult search(Integer page, Integer rows, Seller seller);

    /**
     * 商家详细信息数据回显
     * @param sellerId
     * @return
     */
    public Seller findOne(String sellerId);

    /**
     * 卖家信息审核
     * @param sellerId
     * @param status
     */
    public void updateStatus(String sellerId,String status);
}
