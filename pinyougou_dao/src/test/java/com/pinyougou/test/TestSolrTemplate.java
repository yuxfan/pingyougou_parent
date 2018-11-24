package com.pinyougou.test;

import com.jlb.core.pojo.item.Item;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/applicationContext-solr.xml")
public class TestSolrTemplate {
    @Autowired
    private SolrTemplate solrTemplate;

    @Test
    public void test(){
        HashMap<String, String> searchMap = new HashMap<>();
        searchMap.put("keywords","手机");
        //设置检索条件
        String keywords = searchMap.get("keywords");
        Criteria criteria=new Criteria("item_keywords");
        if (keywords != null && !"".equals(keywords)){
            criteria.is(keywords);   //is  模糊查询
        }

        SimpleQuery query = new SimpleQuery(criteria);
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
        System.out.println(categoryList);
    }
}
