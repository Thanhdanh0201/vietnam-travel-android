package com.example.travel_backend.repository;

import com.example.travel_backend.entity.PostHashtag;
import com.example.travel_backend.entity.PostHashtagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostHashtagRepository extends JpaRepository<PostHashtag, PostHashtagId> {
    void deleteByPost_Id(UUID postId);
}
