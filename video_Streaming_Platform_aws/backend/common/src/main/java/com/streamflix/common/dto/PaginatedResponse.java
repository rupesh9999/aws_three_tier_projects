package com.streamflix.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Paginated response wrapper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {
    
    private List<T> data;
    private int page;
    private int pageSize;
    private int totalPages;
    private long totalItems;
    private boolean hasNext;
    private boolean hasPrevious;
    
    public static <T> PaginatedResponse<T> of(List<T> data, int page, int pageSize, long totalItems) {
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        return PaginatedResponse.<T>builder()
                .data(data)
                .page(page)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .totalItems(totalItems)
                .hasNext(page < totalPages)
                .hasPrevious(page > 1)
                .build();
    }
}
