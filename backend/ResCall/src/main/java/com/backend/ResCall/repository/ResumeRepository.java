package com.backend.ResCall.repository;

import com.backend.ResCall.entity.Resume;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ResumeRepository extends MongoRepository<Resume, String> {
}
