package com.backend.ResCall.controller;

import com.backend.ResCall.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@CrossOrigin(origins = "https://goel-ansh.github.io/ResCall/")
public class ResumeController {

    @Autowired
    ResumeService resumeService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String,String>> uploadResume(@RequestParam("resume") MultipartFile resumeFile  , @RequestParam("jobdesc") String jobdesc) throws IOException {
        System.out.println("📥 Upload endpoint hit");
        System.out.println("📄 Resume file: " + resumeFile.getOriginalFilename());
        System.out.println("📝 Job description: " + jobdesc);
        return resumeService.processResume(resumeFile,jobdesc);
    }
}
