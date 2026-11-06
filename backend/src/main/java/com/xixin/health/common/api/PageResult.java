package com.xixin.health.common.api;

import java.util.List;

public record PageResult<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
    public static <T> PageResult<T> of(List<T> content, int page, int size, long totalElements) {
        int normalizedSize = Math.max(size, 1);
        int totalPages = (int) Math.ceil((double) totalElements / normalizedSize);
        return new PageResult<>(content, page, normalizedSize, totalElements, totalPages);
    }
}
