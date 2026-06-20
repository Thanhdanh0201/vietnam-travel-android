package com.example.travel_backend.dto.request;

import lombok.Data;
import java.util.UUID;

@Data
public class ItineraryNoteRequestDto {
    /** Nội dung ghi chú (bắt buộc). */
    private String content;

    /** URL ảnh đính kèm (tùy chọn). */
    private String imageUrl;

    /**
     * ID của itinerary item mà ghi chú này thuộc về.
     * Null = ghi chú chung của cả lịch trình (hiển thị ở cuối timeline).
     */
    private UUID itineraryItemId;
}
