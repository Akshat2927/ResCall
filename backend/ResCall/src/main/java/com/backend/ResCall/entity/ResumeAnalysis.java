package com.backend.ResCall.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "resume_analysis")
public class ResumeAnalysis {
    @Id
    private String id;
    private String resumeId;
    private String matchScore;
    private List<String> missingKeywords;
    private String suggestions;

}
