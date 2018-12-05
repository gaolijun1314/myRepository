package cn.itcast.core.controller.seller;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.service.seller.SellerService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller")
public class SellerController {
    @Reference
    private SellerService sellerService;

    @RequestMapping("/search.do")
    public PageResult search(Integer page, Integer rows, @RequestBody Seller seller){
       return sellerService.search(page,rows,seller);
    }

    /**
     * 商家详情的数据回显
     * @param id
     * @return
     */
    @RequestMapping("/findOne.do")
    public Seller findOne(String id){
        return sellerService.findOne(id);
    }

    /**
     * 卖家信息审核
     * @param sellerId
     * @param status
     * @return
     */
    @RequestMapping("/updateStatus.do")
    public Result updateStatus(String sellerId, String status){
        try {
            sellerService.updateStatus(sellerId,status);
            return new Result(true,"更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"更新失败");
        }
    }

}
