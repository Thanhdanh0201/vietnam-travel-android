package com.example.travel_backend.dto.request;

import lombok.Data;

@Data
public class UpdateItineraryItemDto {
    /** Ghi chú riêng gắn với địa điểm này trong lịch trình. */
    private String note;
}
