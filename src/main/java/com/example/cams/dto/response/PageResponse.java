package com.example.cams.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Kết quả phân trang.")
public class PageResponse<T> {
    @Schema(description = "Các phần tử của trang hiện tại.")
    private List<T> data;
    @Schema(description = "Tổng số phần tử.")
    private long totalElements;
    @Schema(description = "Tổng số trang.")
    private int totalPages;
    @Schema(description = "Trang hiện tại (bắt đầu từ 0).")
    private int currentPage;
    @Schema(description = "Số phần tử mỗi trang.")
    private int pageSize;
}
