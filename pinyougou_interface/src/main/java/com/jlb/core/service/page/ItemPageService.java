package com.jlb.core.service.page;

public interface ItemPageService {
    /**
     * 生成商品详细页
     */
    public boolean genItemHtml(Long goodsId) throws Exception;
}
