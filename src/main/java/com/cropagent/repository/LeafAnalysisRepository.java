package com.cropagent.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cropagent.entity.LeafAnalysis;
import com.cropagent.entity.User;

public interface LeafAnalysisRepository extends JpaRepository<LeafAnalysis, Long> {
    List<LeafAnalysis> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT l FROM LeafAnalysis l WHERE l.user = :user AND " +
           "(LOWER(l.cropName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.disease) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.analysisType) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<LeafAnalysis> searchByUserAndQuery(@Param("user") User user, @Param("query") String query);
}
