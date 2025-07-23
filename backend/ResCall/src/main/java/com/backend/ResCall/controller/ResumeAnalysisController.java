package com.backend.ResCall.controller;

import com.backend.ResCall.dto.RequestDTO;
import com.backend.ResCall.entity.ResumeAnalysis;
import com.backend.ResCall.service.ResumeAnalysisService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@CrossOrigin(origins = "http://localhost:1234")
public class ResumeAnalysisController {
    private final ResumeAnalysisService analysisService;

    public ResumeAnalysisController(ResumeAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/analyze")
    public Mono<ResumeAnalysis> analyzeResume(@RequestBody RequestDTO request) {
        return analysisService.analyzeAndSave(request.getResumeId());
    }
}
