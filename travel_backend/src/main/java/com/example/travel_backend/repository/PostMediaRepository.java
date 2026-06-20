package com.example.travel_backend.repository;

import com.example.travel_backend.entity.PostMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostMediaRepository extends JpaRepository<PostMedia, UUID> {
    List<PostMedia> findByPostIdInOrderByOrderIndexAsc(List<UUID> postIds);

    void deleteByPost_Id(UUID postId);
}