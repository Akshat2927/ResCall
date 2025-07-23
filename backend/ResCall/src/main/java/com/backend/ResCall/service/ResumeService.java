package com.backend.ResCall.service;

import com.backend.ResCall.entity.Resume;
import com.backend.ResCall.repository.ResumeRepository;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ResumeService {
    @Autowired
    ResumeRepository resumeRepository;
    public ResponseEntity<Map<String,String>> processResume(MultipartFile resumeFile, String jobdesc) throws IOException {
        Map<String , String> response = new HashMap<>();
        try{
            Tika tika = new Tika();
            String resumeText=tika.parseToString(resumeFile.getInputStream());
            Resume data = new Resume();
            data.setResumeText(resumeText);
            data.setJobdesc(jobdesc);
            Resume savedResume = resumeRepository.save(data);
            response.put("resumeId", savedResume.getId());
            response.put("message", "Resume processed and saved successfully");

            return ResponseEntity.ok(response);
        } catch (TikaException e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to process resume: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
