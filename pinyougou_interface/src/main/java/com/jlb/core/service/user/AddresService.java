package com.jlb.core.service.user;

import com.jlb.core.pojo.address.Address;

import java.util.List;

public interface AddresService {

    /**
     * 根据用户id查询地址列表
     * @param userId
     * @return
     */
    public List<Address> findListByLoginName(String userId);
}
