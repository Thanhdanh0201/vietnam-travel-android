package com.example.travel_backend.dto.request;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class DeleteNotificationsRequestDto {
    private List<UUID> ids;
}
