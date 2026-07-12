package com.cropagent.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cropagent.entity.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findAllByOrderByCreatedAtDesc();
}
