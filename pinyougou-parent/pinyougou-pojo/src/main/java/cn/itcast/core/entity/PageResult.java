package cn.itcast.core.entity;

import java.io.Serializable;
import java.util.List;

public class PageResult implements Serializable {
    private Long total;//分页总条数
    private List rows;//分页结果集

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
}
