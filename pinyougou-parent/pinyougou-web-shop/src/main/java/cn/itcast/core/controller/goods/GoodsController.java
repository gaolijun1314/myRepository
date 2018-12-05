package cn.itcast.core.controller.goods;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.goods.GoodsService;
import cn.itcast.core.vo.GoodsVo;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;

    /**
     * 添加商品
     * @param goodsVo
     * @return
     */
    @RequestMapping("/add.do")
    public Result add(@RequestBody GoodsVo goodsVo){
        try {

            //从springsecurity中获取当前登录的商家名
            String name = SecurityContextHolder.getContext().getAuthentication().getName();

            //设置商家名称
            goodsVo.getGoods().setSellerId(name);
            goodsService.add(goodsVo);
            return new Result(true,"保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"保存失败");
        }
    }

    /**
     * 商家商品管理列表分页查询
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    @RequestMapping("/search.do")
    public PageResult search(Integer page, Integer rows, @RequestBody Goods goods){

        return goodsService.search(page,rows,goods);
    }
    @RequestMapping("/findOne.do")
    public GoodsVo findOne(Long id){
        return goodsService.findOne(id);
    }

    /**
     * 商家商品更新
     * @param goodsVo
     * @return
     */
    @RequestMapping("/update.do")
    public Result update(@RequestBody GoodsVo goodsVo){
        try {
            goodsService.update(goodsVo);
            return new Result(true,"更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"更新失败");
        }
    }
}
