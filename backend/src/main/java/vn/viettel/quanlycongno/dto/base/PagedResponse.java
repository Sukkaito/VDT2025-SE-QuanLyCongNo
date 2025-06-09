package vn.viettel.quanlycongno.dto.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private String sortBy;

    public static <T, U> PagedResponse<U> from(Page<T> page, List<U> content) {
        String sortProperty = null;
        if (page.getSort().isSorted()) {
            sortProperty = page.getSort().stream()
                    .findFirst()
                    .map(Sort.Order::getProperty)
                    .orElse(null);
        }

        return new PagedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast(),
                sortProperty
        );
    }
}