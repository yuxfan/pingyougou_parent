package com.jlb.core.service.spec;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.jlb.core.dao.specification.SpecificationDao;
import com.jlb.core.dao.specification.SpecificationOptionDao;
import com.jlb.core.entity.PageResult;
import com.jlb.core.pojo.specification.Specification;
import com.jlb.core.pojo.specification.SpecificationOption;
import com.jlb.core.pojo.specification.SpecificationOptionQuery;
import com.jlb.core.pojo.specification.SpecificationQuery;
import com.jlb.core.vo.SpecificationVo;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class SpecificationServiceImpl implements SpecificationService {

    @Resource
    private SpecificationDao specificationDao;

    @Resource
    private SpecificationOptionDao specificationOptionDao;

    //分页查询列表
    @Override
    public PageResult search(Integer page, Integer rows, Specification specification) {
        PageHelper.startPage(page,rows);
        PageHelper.orderBy("id desc");
        //对象封装的是查询的结果
        SpecificationQuery specificationQuery=new SpecificationQuery();
        if (specification.getSpecName() != null && !"".equals(specification.getSpecName().trim())){
            specificationQuery.createCriteria().andSpecNameLike("%"+specification.getSpecName().trim()+"%");
        }
        Page<Specification> pageList = (Page<Specification>) specificationDao.selectByExample(specificationQuery);
        //返回总条数和结果集
        return new PageResult(pageList.getTotal(),pageList.getResult());
    }

    //查询数据回显   一个规格 对应 多个规格选项
    @Override
    public SpecificationVo findOne(Long id) {
        // 查询规格
        Specification specification = specificationDao.selectByPrimaryKey(id);
        // 查询规格选项
        SpecificationOptionQuery optionQuery = new SpecificationOptionQuery();
        //主外键关联统一
        optionQuery.createCriteria().andSpecIdEqualTo(id);
        List<SpecificationOption> specificationOptionList = specificationOptionDao.selectByExample(optionQuery);
        // 封装数据
        SpecificationVo specificationVo = new SpecificationVo();
        specificationVo.setSpecification(specification);
        specificationVo.setSpecificationOptionList(specificationOptionList);
        return specificationVo;
    }

    //保存
    //SpecificationVo   有specification对象和SpecificationOption对象
    @Transactional
    @Override
    public void add(SpecificationVo specificationVo) {
        //保存规格
        Specification specification = specificationVo.getSpecification();
        //返回自增主键的id   usegenaratedkeys  使用自动生成主键
        specificationDao.insertSelective(specification);
        //保存规格选项
        List<SpecificationOption> specificationOptionList = specificationVo.getSpecificationOptionList();

        if (specificationOptionList != null && specificationOptionList.size()>0 ){

            for (SpecificationOption specificationOption : specificationOptionList) {
                //设置外键   将specification(规格)的id保存到specificationOption(规格选项)中,就是主键和外键统一
                specificationOption.setSpecId(specification.getId());
                //单条插入
             //   specificationOptionDao.insertSelective(specificationOption);
            }
            //批量插入
            specificationOptionDao.insertSelectives(specificationOptionList);
        }
    }

    //更新
    @Transactional
    @Override
    public void update(SpecificationVo specificationVo) {
        // 更新规格
        Specification specification = specificationVo.getSpecification();
        specificationDao.updateByPrimaryKeySelective(specification);
        // 更新规格选项
        // 先删除
        SpecificationOptionQuery optionQuery = new SpecificationOptionQuery();
        optionQuery.createCriteria().andSpecIdEqualTo(specification.getId()); //设置外键
        specificationOptionDao.deleteByExample(optionQuery);
        // 后插入
        List<SpecificationOption> specificationOptionList = specificationVo.getSpecificationOptionList();
        if(specificationOptionList != null && specificationOptionList.size() > 0){
            for (SpecificationOption specificationOption : specificationOptionList) {
                specificationOption.setSpecId(specification.getId()); // 设置外键
            }
            // 批量插入
            specificationOptionDao.insertSelectives(specificationOptionList);
        }
    }

    //删除
    @Transactional
    @Override
    public void dele(Long[] ids) {
        if (ids != null && ids.length>0){
            for (Long id : ids) {
                //删除规格
                specificationDao.deleteByPrimaryKey(id);
                //将查询创造的id封装到SpecificationOptionQuery中并作为删除的例子返回
                SpecificationOptionQuery speQuery = new SpecificationOptionQuery();
                speQuery.createCriteria().andSpecIdEqualTo(id);
                //删除规格选项
                specificationOptionDao.deleteByExample(speQuery);
            }
        }
    }

    //分类模板之规格选项回显
    @Override
    public List<Map<String, String>> selectOptionList() {
        return specificationDao.selectOptionList();
    }
}
