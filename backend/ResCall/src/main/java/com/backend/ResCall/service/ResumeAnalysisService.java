package com.backend.ResCall.service;

import com.backend.ResCall.dto.MatchResponseDTO;
import com.backend.ResCall.entity.Resume;
import com.backend.ResCall.entity.ResumeAnalysis;
import com.backend.ResCall.repository.ResumeAnalysisRepository;
import com.backend.ResCall.repository.ResumeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ResumeAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(ResumeAnalysisService.class);

    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository analysisRepo;
    private final GeminiAIService geminiService;  // ✅ Use Gemini instead of HuggingFace

    public ResumeAnalysisService(
            ResumeRepository resumeRepository,
            ResumeAnalysisRepository analysisRepo,
            GeminiAIService geminiService) {
        this.resumeRepository = resumeRepository;
        this.analysisRepo = analysisRepo;
        this.geminiService = geminiService;
    }

    public Mono<ResumeAnalysis> analyzeAndSave(String resumeId) {

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        String resumeText = resume.getResumeText();
        String jobDescription = resume.getJobdesc();


        return geminiService.analyzeResume(resumeText, jobDescription)
                .map((MatchResponseDTO matchResponse) -> {


                    log.info("🎯 Match Score for Resume [{}]: {}", resumeId, matchResponse.getMatchScore());
                    log.info("📝 Missing Keywords: {}", matchResponse.getMissingKeywords());
                    log.info("💡 Suggestions: {}", matchResponse.getSuggestions());


                    ResumeAnalysis analysis = new ResumeAnalysis();
                    analysis.setResumeId(resume.getId());
                    analysis.setMatchScore(matchResponse.getMatchScore());
                    analysis.setMissingKeywords(matchResponse.getMissingKeywords());
                    analysis.setSuggestions(matchResponse.getSuggestions());

                    return analysisRepo.save(analysis);
                });
    }
}
