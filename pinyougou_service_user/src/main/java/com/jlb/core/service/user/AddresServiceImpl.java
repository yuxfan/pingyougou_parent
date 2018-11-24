package com.jlb.core.service.user;

import com.alibaba.dubbo.config.annotation.Service;
import com.jlb.core.dao.address.AddressDao;
import com.jlb.core.pojo.address.Address;
import com.jlb.core.pojo.address.AddressQuery;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AddresServiceImpl implements AddresService {
    @Resource
    private AddressDao addressDao;

    /**
     * 根据用户查询地址列表
     * @param userId
     * @return
     */
    @Override
    public List<Address> findListByLoginName(String userId) {
        AddressQuery query=new AddressQuery();
        query.createCriteria().andUserIdEqualTo(userId);
        return addressDao.selectByExample(query);
    }
}
