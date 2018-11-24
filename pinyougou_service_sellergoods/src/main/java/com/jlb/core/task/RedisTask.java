package com.jlb.core.task;

import com.alibaba.fastjson.JSON;
import com.jlb.core.dao.item.ItemCatDao;
import com.jlb.core.dao.item.ItemDao;
import com.jlb.core.dao.specification.SpecificationOptionDao;
import com.jlb.core.dao.template.TypeTemplateDao;
import com.jlb.core.pojo.item.ItemCat;
import com.jlb.core.pojo.item.ItemCatQuery;
import com.jlb.core.pojo.specification.SpecificationOption;
import com.jlb.core.pojo.specification.SpecificationOptionQuery;
import com.jlb.core.pojo.template.TypeTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.xml.transform.Templates;
import java.util.List;
import java.util.Map;

@Component
public class RedisTask {

    @Resource
    private ItemDao itemDao;

    @Resource
    private ItemCatDao itemCatDao;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private TypeTemplateDao typeTemplateDao;

    @Resource
    private SpecificationOptionDao specOptionDao;

    // 商品分类的数据同步到缓存中
    // 定义任务 cron：该程序执行的时间     秒分时日月年
    @Scheduled(cron = "00 24 13 * * ?")
    public void autoItemCatsToRedis(){
        List<ItemCat> itemCatList = itemCatDao.selectByExample(null);
        if (itemCatList!=null && itemCatList.size()>0){
            for (ItemCat itemCat : itemCatList) {
                redisTemplate.boundHashOps("itemcat").put(itemCat.getName(),itemCat.getTypeId());
            }
            System.out.println("将商品分类同步到Redis缓存中了");
        }
    }

    // 商品模板的数据同步到缓存中
    // 定义任务
    @Scheduled(cron = "00 24 13 * * ?")
    public void autoTemplateToRedis(){
        List<TypeTemplate> typeTemplates = typeTemplateDao.selectByExample(null);
        if (typeTemplates !=null && typeTemplates.size()>0){
            for (TypeTemplate typeTemplate : typeTemplates) {
                //品牌结果集
                String brandIds = typeTemplate.getBrandIds();
                JSON.parseArray(brandIds,Map.class);
                redisTemplate.boundHashOps("brandIds").put(typeTemplate.getName(),typeTemplate.getId());
               //规格选项结果集
                String specIds = typeTemplate.getSpecIds();
                redisTemplate.boundHashOps("specIds").put(typeTemplate.getName(),typeTemplate.getId());
            }
        }
        System.out.println("商品模板保存到缓存了");
    }

    public List<Map> findBySpecList(Long id) {
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
        String specIds = typeTemplate.getSpecIds(); //获取规格id
        //将json串转换成对象
        List<Map> mapList = JSON.parseArray(specIds, Map.class);
        for (Map map : mapList) {
            //将规格ID转换成long类型
            Long specId = Long.parseLong(map.get("id").toString());
            //规格选项查询条件
            SpecificationOptionQuery optionQuery=new SpecificationOptionQuery();
            optionQuery.createCriteria().andSpecIdEqualTo(specId);
            List<SpecificationOption> options = specOptionDao.selectByExample(optionQuery);
            map.put("options",options); //将查询到的规格选项存放到map集合
        }
        return mapList;
    }
}
