package com.backend.ResCall.repository;

import com.backend.ResCall.entity.ResumeAnalysis;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ResumeAnalysisRepository extends MongoRepository<ResumeAnalysis , String> {
}
