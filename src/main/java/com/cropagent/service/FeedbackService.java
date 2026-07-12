package com.cropagent.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cropagent.dto.FeedbackRequest;
import com.cropagent.entity.Feedback;
import com.cropagent.repository.FeedbackRepository;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    // Constructor Injection
    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public Feedback saveFeedback(FeedbackRequest req) {
        Feedback fb = new Feedback();
        fb.setName(req.getName());
        fb.setEmail(req.getEmail());
        fb.setMessage(req.getMessage());
        return feedbackRepository.save(fb);
    }

    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc();
    }

    public void deleteFeedback(Long id) {
        feedbackRepository.deleteById(id);
    }
}