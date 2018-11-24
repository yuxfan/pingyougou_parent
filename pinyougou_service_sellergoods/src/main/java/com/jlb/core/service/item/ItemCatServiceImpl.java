package com.jlb.core.service.item;

import com.alibaba.dubbo.config.annotation.Service;
import com.jlb.core.dao.item.ItemCatDao;
import com.jlb.core.pojo.item.ItemCat;
import com.jlb.core.pojo.item.ItemCatQuery;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class ItemCatServiceImpl implements ItemCatService {

    @Resource
    private ItemCatDao itemCatDao;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;


    /**
     * 商品列表分类等级查询;根据父节点查询其下的子节点
     * 因为数据量比较少,所以不需要分页,直接查询即可
     * @param parentId
     * @return
     */
    @Override
    public List<ItemCat> findByParentId(Long parentId) {
        //查询所有分类放入缓存中
        List<ItemCat> itemCats = itemCatDao.selectByExample(null);
        if (itemCats != null && itemCats.size()>0){
            for (ItemCat itemCat : itemCats) {
                //根据名字找模板id,存入缓存中
                redisTemplate.boundHashOps("itemCat").put(itemCat.getName(),itemCat.getTypeId());
            }
        }
        //设置查询条件   因为结果为集合,所以需要封装到ItemCatQuery
        ItemCatQuery itemCatQuery=new ItemCatQuery();
        itemCatQuery.createCriteria().andParentIdEqualTo(parentId);
        //根据实例查询(itemCatQuery作为参数)
        List<ItemCat> itemCatList = itemCatDao.selectByExample(itemCatQuery);
        return itemCatList;
    }

    //根据三级分类查询模板id
    @Override
    public ItemCat findOne(Long id) {
        return itemCatDao.selectByPrimaryKey(id);
    }

    //查询所有分类列表
    @Override
    public List<ItemCat> findAll() {
        return itemCatDao.selectByExample(null);
    }
}
