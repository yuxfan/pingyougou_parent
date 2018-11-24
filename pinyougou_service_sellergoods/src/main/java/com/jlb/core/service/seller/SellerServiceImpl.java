package com.jlb.core.service.seller;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.jlb.core.dao.seller.SellerDao;
import com.jlb.core.entity.PageResult;
import com.jlb.core.pojo.seller.Seller;
import com.jlb.core.pojo.seller.SellerQuery;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class SellerServiceImpl implements SellerService {

    @Resource
    private SellerDao sellerDao;

    @Transactional
    @Override
    public void add(Seller seller) {
        //密码加密
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(seller.getPassword());
        seller.setPassword(encode);
        //状态 默认0 关闭
        seller.setStatus("0");
        //提交日期
        seller.setCreateTime(new Date());
        sellerDao.insertSelective(seller);
    }

    //查询所有,条件查询和分页于一体
    @Override
    public PageResult search(Integer page,Integer rows,Seller seller) {
        PageHelper.startPage(page,rows);
        SellerQuery sellerQuery=new SellerQuery();
        SellerQuery.Criteria criteria = sellerQuery.createCriteria();

        if (seller.getStatus()!=null && !"".equals(seller.getStatus().trim())){
            criteria.andStatusEqualTo(seller.getStatus().trim());
        }
        /*
        模糊查询一定不要忘记拼接%
        * 另外模糊查询名字的话,需要使用like
        * 数字或字母查询可以使用EqualTo
        * */
        if (seller.getName()!=null && !"".equals(seller.getName().trim())){
            criteria.andNameLike("%"+seller.getName().trim()+"%");
        }

        //使用EqualTo  报错syntactically incorrect(语法不正确)
        if (seller.getNickName()!=null && !"".equals(seller.getNickName().trim())){
            criteria.andNickNameLike("%"+seller.getNickName().trim()+"%");
        }
        Page<Seller> p = (Page<Seller>) sellerDao.selectByExample(sellerQuery);
        return new PageResult(p.getTotal(),p.getResult());
    }

    //查询实体(待审核商家详情)
    @Override
    public Seller findOne(String sellerId) {
        return sellerDao.selectByPrimaryKey(sellerId);
    }

    //对商家进行审核(根据id更新状态)
    @Transactional
    @Override
    public void updateStatus(String sellerId, String status) {
        Seller seller=new Seller();
        seller.setSellerId(sellerId);
        seller.setStatus(status);
        sellerDao.updateByPrimaryKeySelective(seller);
    }
}
