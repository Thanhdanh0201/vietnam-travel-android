package com.example.travel_backend.dto.request;

import lombok.Data;
import java.util.UUID;

@Data
public class RepostRequestDto {
    private UUID postId;
    private String quoteText;
}