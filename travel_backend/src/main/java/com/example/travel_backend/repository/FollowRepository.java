package com.example.travel_backend.repository;

import com.example.travel_backend.entity.Follow;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<Follow, String> {
    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    @Transactional
    void deleteByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    @EntityGraph(attributePaths = {"follower"})
    List<Follow> findByFollowingIdOrderByCreatedAtDesc(UUID followingId);

    @EntityGraph(attributePaths = {"following"})
    List<Follow> findByFollowerIdOrderByCreatedAtDesc(UUID followerId);
}
