package com.jlb.core.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.jlb.core.dao.good.BrandDao;
import com.jlb.core.entity.PageResult;
import com.jlb.core.pojo.good.Brand;
import com.jlb.core.pojo.good.BrandQuery;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class BrandServiceImpl implements BrandService  {
    @Resource
    private BrandDao brandDao;
    @Override
    public List<Brand> findAll() {
        return brandDao.selectByExample(null);
    }

    @Override
    //PageResult对象中有结果集和总条数
    public PageResult findPage(Integer pageNum, Integer pageSize) throws Exception {
        //分页的条件(开始分页(当前页,每页条数))
        PageHelper.startPage(pageNum,pageSize);
        //查询所有的品牌列表
        Page<Brand> page = (Page<Brand>) brandDao.selectByExample(null);
        //将结果封装到pageResult中
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public PageResult search(Integer pageNum, Integer pageSize, Brand brand) throws Exception {

        //分页的条件
        PageHelper.startPage(pageNum,pageSize);
        //根据id降序
        PageHelper.orderBy("id desc");
        //设置查询条件   用来封装查询条件的
        BrandQuery brandQuery=new BrandQuery();
        //创造条件   BrandQuery中封装条件的对象
        BrandQuery.Criteria criteria = brandQuery.createCriteria();
        //封装品牌的名称
        //判断名字不为空   名字不为空,且不为空字符串且去空格
        if (brand.getName() != null && !"".equals(brand.getName().trim())){
            //拼接sql语句
            criteria.andNameLike("%"+brand.getName().trim()+"%");
        }
        //trim   去空格
        if (brand.getFirstChar() != null && !"".equals(brand.getFirstChar().trim())){
            criteria.andFirstCharEqualTo(brand.getFirstChar().trim());
        }
        //根据id降序  asc升序(默认的)
        //brandQuery.setOrderByClause("id desc");
        /*
        查询所有的品牌列表
        page 继承ArrayList,是com.github.pagehelper包下的
        */
        Page<Brand> page = (Page<Brand>) brandDao.selectByExample(brandQuery);
        //将结果封装到pageResult中返回
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Transactional
    @Override
    public void add(Brand brand) throws Exception {
        brandDao.insertSelective(brand);
    }

    //更新 之 先查询
    @Override
    public Brand findOne(Long id) throws Exception {
        return brandDao.selectByPrimaryKey(id);
    }

    /* 更新 */
    @Transactional
    @Override
    public void update(Brand brand) {
        brandDao.updateByPrimaryKeySelective(brand);
    }

    //删除
    @Transactional
    @Override
    public void delete(Long[] ids) {
        if (ids != null && ids.length>0){
            //这个循环方法原理是一个一个删除的，效率较低
            /*for (Long id : ids) {
                brandDao.deleteByPrimaryKey(id);
            }*/
            //自定义方法,批量删除
            brandDao.deleteByPrimaryKeys(ids);
        }
    }

    //查询分类模板关联的品牌列表
    @Override
    public List<Map<String, String>> selectOptionList() {

        return brandDao.selectOptionList();
    }
}
