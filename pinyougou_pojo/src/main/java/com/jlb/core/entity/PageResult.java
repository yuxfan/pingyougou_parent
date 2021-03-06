package com.jlb.core.entity;

import java.io.Serializable;
import java.util.List;

public class PageResult implements Serializable {

    private Long total;        //总条数
    private List rows;          //结果集  不确定类型,所以返回类型空着

    public PageResult(Long total, List rows) {
        this.total = total;
        this.rows = rows;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List getRows() {
        return rows;
    }

    public void setRows(List rows) {
        this.rows = rows;
    }

    @Override
    public String toString() {
        return "PageResult{" +
                "total=" + total +
                ", rows=" + rows +
                '}';
    }
}
