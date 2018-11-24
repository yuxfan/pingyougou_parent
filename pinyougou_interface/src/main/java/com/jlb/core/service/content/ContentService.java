package com.jlb.core.service.content;

import java.util.List;

import com.jlb.core.entity.PageResult;
import com.jlb.core.pojo.ad.Content;
import com.jlb.core.entity.PageResult;
import com.jlb.core.pojo.ad.Content;

public interface ContentService {

	public List<Content> findAll();

	public PageResult findPage(Content content, Integer pageNum, Integer pageSize);

	public void add(Content content);

	public void edit(Content content);

	public Content findOne(Long id);

	public void delAll(Long[] ids);

	/**
	 * 查询该分类下的广告列表
	 * @param categoryId
	 * @return
	 */
	public List<Content> findByCategoryId(Long categoryId);

}
