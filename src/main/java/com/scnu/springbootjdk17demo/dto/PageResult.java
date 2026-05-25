package com.scnu.springbootjdk17demo.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class PageResult<T> {
    private List<T> records;
    private long total;
    private long size;
    private long current;

    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getSize(), page.getCurrent());
    }
}