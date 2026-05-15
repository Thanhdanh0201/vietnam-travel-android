package com.example.travel_backend.service.impl;

import com.example.travel_backend.dto.request.ReportRequestDto;
import com.example.travel_backend.entity.Report;
import com.example.travel_backend.repository.CommentRepository;
import com.example.travel_backend.repository.PostRepository;
import com.example.travel_backend.repository.ReportRepository;
import com.example.travel_backend.repository.UserRepository;
import com.example.travel_backend.service.ReportService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;

    @Override
    @Transactional
    public void createReport(UUID reporterId, ReportRequestDto request) {
        System.out.println("Creating new report from user: " + reporterId);

        Report report = new Report();
        report.setReporter(userRepository.getReferenceById(reporterId));
        report.setReason(request.getReason());
        report.setDescription(request.getDescription());

        if (request.getReportedPostId() != null) {
            report.setReportedPost(postRepository.getReferenceById(request.getReportedPostId()));
        }
        if (request.getReportedCommentId() != null) {
            report.setReportedComment(commentRepository.getReferenceById(request.getReportedCommentId()));
        }
        if (request.getReportedUserId() != null) {
            report.setReportedUser(userRepository.getReferenceById(request.getReportedUserId()));
        }

        reportRepository.save(report);
    }
}