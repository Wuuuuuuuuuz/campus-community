package com.campus.community.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PageResult<T> {

    private List<T> records;
    private long total;
    private int page;
    private int size;
    private int pages;

    public static <T> PageResult<T> of(List<T> records, long total, int page, int size) {
        int pages = (int) Math.ceil((double) total / size);
        return new PageResult<>(records, total, page, size, pages);
    }
}
