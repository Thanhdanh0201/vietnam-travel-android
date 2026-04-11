package com.example.travel_backend.dto.response;

import lombok.Data;
import java.util.UUID;

@Data
public class PhotoDto {
    private UUID id;
    private String photo_url;
    private Boolean is_primary;
}