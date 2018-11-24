package com.jlb.core.service.search;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.jlb.core.dao.item.ItemDao;
import com.jlb.core.pojo.item.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import org.springframework.security.core.userdetails.UserDetailsService;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {
    @Resource
    private ItemDao itemDao;

    @Resource
    private SolrTemplate solrTemplate;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    /**
     * 前台系统检索
     * @param searchMap
     * @return
     */
    //前台传递过来的数据封装成了一个集合 searchMap   下边的需要的数据是从这里获取的
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        //把一个个小的业务集合,存放在这个大集合中   用于响应前台请求
        Map<String,Object> resultMap = new HashMap<>();

        //处理输入的关键字中包含的空格
        String keywords = searchMap.get("keywords");
        if (keywords != null && !"".equals(keywords)){
            keywords=keywords.replace(" ", "");
            searchMap.put("keywords",keywords);
        }

        //Map<String, Object> map = searchForPage(searchMap);
        Map<String, Object> map = searchForHighLightPage(searchMap);
        List<String> categoryList = searchForGroupPage(searchMap);
        if (categoryList != null && categoryList.size()>0){
            //默认加载分类下的第一个品牌及规格
            Map<String,Object> brandSpecCat = searchBrandAndSpecListByCategory(categoryList.get(0));
            resultMap.putAll(brandSpecCat);
            resultMap.put("categoryList",categoryList);
        }
        resultMap.putAll(map);    //putAll:  可以存放集合
        return resultMap;
    }
    //默认加载分类下的第一个品牌及规格
    private  Map<String,Object> searchBrandAndSpecListByCategory(String name) {
        //根据分类获取模板id
        Object typeId = redisTemplate.boundHashOps("itemCat").get(name);
        //根据模板id获取品牌和规格
        //获取商品品牌   这个是需要从缓存中获取的,所以需要根据存的时候的键来获取模板id  (重点:存取键一致)
        List<Map> brandList =(List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
        //获取商品规格
        List<Map> specList =(List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
        //封装结果
        Map<String,Object> map=new HashMap<>();
        map.put("brandList", brandList);
        map.put("specList",specList);
        return map;
    }

    //查询商品的分类
    private List<String> searchForGroupPage(Map<String,String> searchMap) {
        //设置检索条件
        String keywords = searchMap.get("keywords");
        Criteria criteria=new Criteria("item_keywords");
        if (keywords != null && !"".equals(keywords)){
            criteria.is(keywords);   //is  模糊查询
        }
        //封装查询条件
        SimpleQuery query = new SimpleQuery(criteria);
        //设置分组条件
        GroupOptions groupOptions = new GroupOptions();
        groupOptions.addGroupByField("item_category");   //设置分组字段
        query.setGroupOptions(groupOptions);

        GroupPage<Item> groupPage = solrTemplate.queryForGroupPage(query, Item.class);

        //处理结果集
        List<String> categoryList = new ArrayList<>();
        GroupResult<Item> groupResult = groupPage.getGroupResult("item_category");  //获取需要的分组结果集
        Page<GroupEntry<Item>> groupEntries = groupResult.getGroupEntries();
        for (GroupEntry<Item> groupEntry : groupEntries) {
            String groupValue = groupEntry.getGroupValue();
            //分类结果集放入集合中
            categoryList.add(groupValue);
        }
        return categoryList;
    }

    //关键字检索高亮并分页
    private Map<String, Object> searchForHighLightPage(Map<String, String> searchMap) {
        //一,设置检索条件
        //获取关键字
        String keywords = searchMap.get("keywords");
        Criteria criteria = new Criteria("item_keywords");  //那个字段作为条件
        if (keywords != null && !"".equals(keywords)){
            criteria.is(keywords);    //is  模糊查询
        }
        //封装查询条件
        SimpleHighlightQuery query = new SimpleHighlightQuery(criteria);

        //分页条件
        //当前页转换成integer类型
        Integer pageNo = Integer.valueOf(searchMap.get("pageNo"));
        Integer pageSize = Integer.valueOf(searchMap.get("pageSize"));
        Integer offset=(pageNo-1)*pageSize;
        query.setOffset(offset);     //起始行
        query.setRows(pageSize);    //每页显示条数
        // 设置关键字高亮:对检索的内容添加HTML的标签
        HighlightOptions highlightOptions =new HighlightOptions();
        highlightOptions.addField("item_title");       //对那个字段进行高亮
        highlightOptions.setSimplePrefix("<font color='red'>");  //开始标签
        highlightOptions.setSimplePostfix("</font>");    //结束标签
        query.setHighlightOptions(highlightOptions);

        //添加条件过滤
        //根据分类
        String category = searchMap.get("category");
        if (category!=null && !"".equals(category)){
            Criteria cri=new Criteria("item_category");
            cri.is(category);
            FilterQuery filterQuery=new SimpleFilterQuery(cri);
            query.addFilterQuery(filterQuery);
        }
        //根据品牌
        String brand = searchMap.get("brand");
        if ( brand!=null && !"".equals(brand)){
            Criteria cri=new Criteria("item_brand");
            cri.is(brand);
            FilterQuery filterQuery=new SimpleFilterQuery(cri);
            query.addFilterQuery(filterQuery);
        }
        //根据规格
        String spec = searchMap.get("spec");
        if (spec !=null && !"".equals(spec)){
            //json转换成对象,放入到map集合中
            Map<String,String> map = JSON.parseObject(spec, Map.class);
            Set<Map.Entry<String, String>> entries = map.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                Criteria cri = new Criteria("item_spec_" + entry.getKey());
                //集合中的值 {"机身内存"(key):"16G"(value),"网络":"联通3G"}
                cri.is(entry.getValue());
                FilterQuery filterQuery=new SimpleFilterQuery(cri);
                query.addFilterQuery(filterQuery);
            }
        }
        //根据价格
        //searchMap是一个前台传递过来的查询实体,price是其中的一项(是用户点击的价格区间值)
        String price = searchMap.get("price");
        if (price != null && !"".equals(price)){
            //将价格"0-500" 切割成数组[0],[1]
            String[] prices=price.split("-");
            Criteria cri=new Criteria("item_price");
            //包含索引0和1的值
            cri.between(prices[0],prices[1],true,true);
            FilterQuery filterQuery=new SimpleFilterQuery(cri);
            query.addFilterQuery(filterQuery);
        }
        //排序    新品  价格
        //根据新品排序   sort  排序规则   sortField: 排序字段
        String s = searchMap.get("sort");
        if (s != null && !"".equals(s)){
            //如果是升序,就执行这个
            if ("ASC".equals(s)){
                Sort sort=new Sort(Sort.Direction.ASC,"item_"+searchMap.get("sortField"));
                query.addSort(sort);
            }else {   //否则就执行这个降序
                Sort sort=new Sort(Sort.Direction.DESC,"item_"+searchMap.get("sortField"));
                query.addSort(sort);
            }
        }

        //根据条件查询分页
        HighlightPage<Item> highlightPage = solrTemplate.queryForHighlightPage(query, Item.class);
        //处理高亮的结果
        List<HighlightEntry<Item>> highlighted = highlightPage.getHighlighted();
        if (highlighted != null && highlighted.size()>0){
            for (HighlightEntry<Item> itemHighlightEntry : highlighted) {
                Item item = itemHighlightEntry.getEntity();   //普通的结果
                //高亮的结果
                List<HighlightEntry.Highlight> highlights = itemHighlightEntry.getHighlights();
                if (highlights != null && highlights.size()>0) {
                    for (HighlightEntry.Highlight highlight : highlights) {
                        String title = highlight.getSnipplets().get(0);
                        item.setTitle(title);
                    }
                }
            }
        }
        //二,将结果封装到集合
        //处理结果
        Map<String,Object> map=new HashMap<>();
        map.put("totalPages",highlightPage.getTotalPages());   //总页数
        map.put("total",highlightPage.getTotalElements());     //总条数
        map.put("rows",highlightPage.getContent());            //结果集
        return map;
    }


    //根据关键字查询并分页  因为数据库中的一部分图片是从网上扒的,所以需要联网,不联网找不到地址,不显示图片
    private Map<String, Object> searchForPage(Map<String, String> searchMap) {
        //一,设置检索条件
        //获取关键字
        String keywords = searchMap.get("keywords");
        Criteria criteria = new Criteria("item_keywords");
        if (keywords != null && !"".equals(keywords)){
            criteria.is(keywords);    //is  模糊查询
        }
        //封装查询条件
        SimpleQuery query = new SimpleQuery(criteria);
        //分页条件
        //当前页转换成integer类型
        Integer pageNo = Integer.valueOf(searchMap.get("pageNo"));
        Integer pageSize = Integer.valueOf(searchMap.get("pageSize"));
        Integer offset=(pageNo-1)*pageSize;
        query.setOffset(offset);     //起始行
        query.setRows(pageSize);    //每页显示条数
        //根据条件查询分页
        ScoredPage<Item> scoredPage = solrTemplate.queryForPage(query, Item.class);
        //二,将结果封装到集合
        //处理结果
        Map<String,Object> map=new HashMap<>();
        map.put("totalPages",scoredPage.getTotalPages());   //总页数
        map.put("total",scoredPage.getTotalElements());     //总条数
        map.put("rows",scoredPage.getContent());            //结果集
        return map;
    }
}
