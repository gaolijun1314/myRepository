package cn.itcast.core.service.staticpage;

public interface StaticPageService {
    /**
     * 商品审核通过后生成静态页接口
     * @param id
     */
    public void getHtml(Long id);
}
