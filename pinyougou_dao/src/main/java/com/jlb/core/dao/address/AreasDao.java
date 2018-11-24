package com.jlb.core.dao.address;

import com.jlb.core.pojo.address.Areas;
import com.jlb.core.pojo.address.AreasQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AreasDao {
    int countByExample(AreasQuery example);

    int deleteByExample(AreasQuery example);

    int deleteByPrimaryKey(Integer id);

    int insert(Areas record);

    int insertSelective(Areas record);

    List<Areas> selectByExample(AreasQuery example);

    Areas selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") Areas record, @Param("example") AreasQuery example);

    int updateByExample(@Param("record") Areas record, @Param("example") AreasQuery example);

    int updateByPrimaryKeySelective(Areas record);

    int updateByPrimaryKey(Areas record);
}