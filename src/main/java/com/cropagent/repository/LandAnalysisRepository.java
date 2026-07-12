package com.cropagent.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cropagent.entity.LandAnalysis;
import com.cropagent.entity.User;

public interface LandAnalysisRepository extends JpaRepository<LandAnalysis, Long> {

    List<LandAnalysis> findByUserOrderByCreatedAtDesc(User user);

    @Query("""
        SELECT l
        FROM LandAnalysis l
        WHERE l.user = :user
        AND (
            LOWER(l.cropName) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(l.state) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(l.district) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(l.suitableSeason) LIKE LOWER(CONCAT('%', :query, '%'))
        )
    """)
    List<LandAnalysis> searchByUserAndQuery(
            @Param("user") User user,
            @Param("query") String query
    );
}