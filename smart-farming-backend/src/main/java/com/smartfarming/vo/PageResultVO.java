package com.smartfarming.vo;

import lombok.Data;

import java.util.List;

@Data
public class PageResultVO<T> {
    private List<T> list;
    private Long total;
    private Long page;
    private Long size;

    public PageResultVO() {}

    public PageResultVO(List<T> list, Long total, Long page, Long size) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.size = size;
    }
}
