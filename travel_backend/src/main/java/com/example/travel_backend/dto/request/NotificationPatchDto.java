package com.example.travel_backend.dto.request;

import lombok.Data;

@Data
public class NotificationPatchDto {
    private Boolean isRead;
    private Boolean isDeleted;
}