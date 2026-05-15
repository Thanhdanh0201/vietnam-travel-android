package com.example.travel_backend.dto.request;

import lombok.Data;
import java.util.UUID;

@Data
public class SyncUserRequestDto {
    private UUID id;
    private String email;
    private String name;
}
