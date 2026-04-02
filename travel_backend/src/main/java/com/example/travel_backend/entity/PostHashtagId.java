package com.example.travel_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class PostHashtagId implements Serializable {
    private static final long serialVersionUID = 3207988735575420553L;
    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Column(name = "hashtag_id", nullable = false)
    private UUID hashtagId;


}