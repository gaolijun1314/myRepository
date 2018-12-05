package cn.itcast.core.service.content;

import java.util.List;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.ad.ContentQuery;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import cn.itcast.core.dao.ad.ContentDao;
import cn.itcast.core.pojo.ad.Content;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class ContentServiceImpl implements ContentService {

    @Resource
    private ContentDao contentDao;
    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public List<Content> findAll() {
        List<Content> list = contentDao.selectByExample(null);
        return list;
    }

    @Override
    public PageResult findPage(Content content, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<Content> page = (Page<Content>) contentDao.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Transactional
    @Override
    public void add(Content content) {
        //先清除缓存
        clearCache(content.getCategoryId());
        //更新广告
        contentDao.insertSelective(content);
    }

    @Transactional
    @Override
    public void edit(Content content) {
        //获取新的广告分类id
        Long newCategoryId = content.getCategoryId();
        //获取旧的广告分类id
        Long oldCategoryId = contentDao.selectByPrimaryKey(content.getId()).getCategoryId();

        if (newCategoryId != oldCategoryId) {
            clearCache(newCategoryId);
            clearCache(oldCategoryId);
        } else {
            clearCache(oldCategoryId);
        }

        contentDao.updateByPrimaryKeySelective(content);
    }

    @Override
    public Content findOne(Long id) {
        Content content = contentDao.selectByPrimaryKey(id);
        return content;
    }

    @Transactional
    @Override
    public void delAll(Long[] ids) {
        if (ids != null) {
            for (Long id : ids) {
                //清除缓存
                clearCache(contentDao.selectByPrimaryKey(id).getCategoryId());
                contentDao.deleteByPrimaryKey(id);
            }
        }
    }

    /**
     * 首页大广告轮播
     *
     * @param categoryId
     * @return
     */
    @Override
    public List<Content> findByCategoryId(Long categoryId) {
        //先从缓存中取数据
        List<Content> list = (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);

        if (list == null) {
            //同步锁,防止缓存穿透
            synchronized (this) {
                //先从缓存中取数据
                list = (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);
                if (list == null) {
                    ContentQuery contentQuery = new ContentQuery();
                    contentQuery.createCriteria().andCategoryIdEqualTo(categoryId).andStatusEqualTo("1");
                    list = contentDao.selectByExample(contentQuery);
                    //将数据放入缓存
                    redisTemplate.boundHashOps("content").put(categoryId, list);
                }
            }
        }
        return list;
    }

    private void clearCache(Long categoryId) {
        redisTemplate.boundHashOps("content").delete(categoryId);
    }
}
