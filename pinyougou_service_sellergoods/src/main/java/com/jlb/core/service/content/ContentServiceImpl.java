package com.jlb.core.service.content;

import java.util.List;


import com.jlb.core.dao.ad.ContentDao;
import com.jlb.core.entity.PageResult;
import com.jlb.core.pojo.ad.Content;
import com.jlb.core.pojo.ad.ContentQuery;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service
public class ContentServiceImpl implements ContentService {

	@Resource
	private ContentDao contentDao;

	@Resource
	private RedisTemplate<String,Object> redisTemplate;

	@Override
	public List<Content> findAll() {
		List<Content> list = contentDao.selectByExample(null);
		return list;
	}

	@Override
	public PageResult findPage(Content content, Integer pageNum, Integer pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<Content> page = (Page<Content>)contentDao.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}
    @Transactional
	@Override
	public void add(Content content) {
	    //新增时数据发生变化,先删除之前的
        clearCache(content.getCategoryId());
        //在插入新的
	    contentDao.insertSelective(content);
	}
    //创建私有方法,清除缓存
	private void clearCache(Long categoryId){
	    redisTemplate.boundHashOps("content").delete(categoryId);
    }

    @Transactional
	@Override
	public void edit(Content content) {
	    //清除缓存
        //思路:判断广告是否发生改变  不改变:删除旧的, 改变:本次和之前的都删除
        Long newCategoryId = content.getCategoryId();
        Long oldCategoryId = contentDao.selectByPrimaryKey(content.getId()).getCategoryId();  //在更新之前查询
        if (newCategoryId != oldCategoryId){
            //分类改变了,全部清空
            clearCache(newCategoryId);
            clearCache(oldCategoryId);
        }else {
            clearCache(oldCategoryId);
        }
        //更新数据
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
		if(ids != null){
			for(Long id : ids){
			    //清除缓存
                //先根据id获取广告对象
                Content content = contentDao.selectByPrimaryKey(id);
                //然后根据分类id清除
                clearCache(content.getCategoryId());
                contentDao.deleteByPrimaryKey(id);
			}
		}
	}

	/**
	 * 查询该分类下的广告列表
	 * @param categoryId
	 * @return
	 */
	@Override
	public List<Content> findByCategoryId(Long categoryId) {
	    //首先判断缓存中是否有数据
        List<Content> list = (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);
        //如果缓存中没有,
        if (list == null){
            //添加锁机制(排队)   解决缓存穿透
            synchronized (this) {
                //二次校验
                list = (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);
                if (list == null) {
                    //就从数据库中去取
                    ContentQuery contentQuery = new ContentQuery();
                    contentQuery.createCriteria().andCategoryIdEqualTo(categoryId);
                    list = contentDao.selectByExample(contentQuery);
                    //添加到缓存中
                    redisTemplate.boundHashOps("content").put(categoryId, list);
                }
            }
        }
		return list;
	}
}
