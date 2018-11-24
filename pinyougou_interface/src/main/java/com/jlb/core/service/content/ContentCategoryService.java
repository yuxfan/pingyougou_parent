package com.jlb.core.service.content;

import java.util.List;

import com.jlb.core.entity.PageResult;
import com.jlb.core.pojo.ad.ContentCategory;

public interface ContentCategoryService {

public List<ContentCategory> findAll();
	
	public PageResult findPage(ContentCategory contentCategory, Integer pageNum, Integer pageSize);
	
	public void add(ContentCategory contentCategory);
	
	public void edit(ContentCategory contentCategory);
	
	public ContentCategory findOne(Long id);
	
	public void delAll(Long[] ids);


}
