package com.atlas.platform.util;

import com.atlas.platform.dto.PagedResponse;
import java.util.List;

public final class PaginationUtils {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    private PaginationUtils() {
    }

    public static boolean requested(Integer page, Integer size) {
        return page != null || size != null;
    }

    public static <T> PagedResponse<T> page(List<T> items, Integer page, Integer size) {
        int normalizedPage = page == null ? DEFAULT_PAGE : Math.max(page, 0);
        int normalizedSize = size == null ? DEFAULT_SIZE : Math.max(1, Math.min(size, MAX_SIZE));
        int totalElements = items.size();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / normalizedSize);
        int fromIndex = Math.min(normalizedPage * normalizedSize, totalElements);
        int toIndex = Math.min(fromIndex + normalizedSize, totalElements);

        return new PagedResponse<>(
                items.subList(fromIndex, toIndex),
                normalizedPage,
                normalizedSize,
                totalElements,
                totalPages,
                normalizedPage == 0,
                totalPages == 0 || normalizedPage >= totalPages - 1
        );
    }
}
