package com.jlb.core.service.template;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.jlb.core.dao.specification.SpecificationOptionDao;
import com.jlb.core.dao.template.TypeTemplateDao;
import com.jlb.core.entity.PageResult;
import com.jlb.core.pojo.specification.SpecificationOption;
import com.jlb.core.pojo.specification.SpecificationOptionQuery;
import com.jlb.core.pojo.template.TypeTemplate;
import com.jlb.core.pojo.template.TypeTemplateQuery;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

    @Resource
    private TypeTemplateDao typeTemplateDao;

    @Resource
    private SpecificationOptionDao specOptionDao;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;


    @Override
    public PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate) {
        List<TypeTemplate> list = typeTemplateDao.selectByExample(null);
        if (list != null && list.size()>0){
            for (TypeTemplate template : list) {
                //缓存改模板下的品牌
                List<Map> brandList= JSON.parseArray(template.getBrandIds(),Map.class);
                //使用hash数据结构保存
                redisTemplate.boundHashOps("brandList").put(template.getId(),brandList);
                //缓存该模板下的规格
                List<Map> specList = findBySpecList(template.getId());
                redisTemplate.boundHashOps("specList").put(template.getId(),specList);
            }
        }

        //分页条件
        PageHelper.startPage(page,rows);
        TypeTemplateQuery typeTemplateQuery=new TypeTemplateQuery();
        //封装条件
        if (typeTemplate.getName() != null && !"".equals(typeTemplate.getName().trim())){
            typeTemplateQuery.createCriteria().andNameLike("%"+typeTemplate.getName().trim()+"%");
        }
        //将封装好的typeTemplateQuery作为查询的条件
        Page<TypeTemplate> p = (Page<TypeTemplate>) typeTemplateDao.selectByExample(typeTemplateQuery);
        //返回结果集
        return new PageResult(p.getTotal(),p.getResult());
    }

    //保存
    @Transactional
    @Override
    public void add(TypeTemplate typeTemplate) {
        typeTemplateDao.insertSelective(typeTemplate);
    }

    //更新前  之 回显
    @Override
    public TypeTemplate findOne(Long id) {
        return typeTemplateDao.selectByPrimaryKey(id);
    }

    //更新
    @Override
    public void update(TypeTemplate typeTemplate) {
        typeTemplateDao.updateByPrimaryKeySelective(typeTemplate);
    }

    //自定义批量删除
    @Override
    public void delete(Long[] ids) {
        if(ids != null && ids.length>0){
            typeTemplateDao.deleteByPrimaryKeys(ids);
        }
    }

    //查询出模板id后,加载其下的规格及规格选项
    @Override
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
