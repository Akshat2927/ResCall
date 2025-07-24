package com.backend.ResCall.service;

import com.backend.ResCall.dto.MatchResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class GeminiAIService {

    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public GeminiAIService(@Value("${gemini.api.key}") String geminiApiKey) {


        String geminiApiUrl =
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=" + geminiApiKey;

        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(20))
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);

        this.webClient = WebClient.builder()
                .baseUrl(geminiApiUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    public Mono<MatchResponseDTO> analyzeResume(String resumeText, String jobDescription) {
        String prompt =
                "You are an ATS (Applicant Tracking System) evaluator that is EXTREMELY STRICT.\n\n" +

                        "Compare the RESUME and JOB DESCRIPTION ONLY based on:\n" +
                        "- Exact matching of required skills, tools, technologies, certifications.\n" +
                        "- Relevant years of experience.\n" +
                        "- Specific job titles or domain expertise.\n" +
                        "- Soft skills ONLY if explicitly mentioned in the job description.\n\n" +

                        "SCORING RULES (VERY STRICT):\n" +
                        "1. If the job description is vague, too short (< 15 words), or meaningless → give a LOW score (10%–20%).\n" +
                        "2. If only a few skills match → give 30%–50%.\n" +
                        "3. If about half of the required skills & experience match → give 50%–70%.\n" +
                        "4. Only give 90%+ if the resume *highly* matches almost ALL required skills, tools, and responsibilities.\n" +
                        "5. NEVER give a high score for random text like 'abcd'.\n\n" +

                        "IMPORTANT:\n" +
                        "- If the job description does not clearly mention any job-related keywords, give LOWEST possible score.\n" +
                        "- Be realistic and conservative. Do NOT inflate the score.\n\n" +

                        "RESUME:\n" + resumeText + "\n\n" +
                        "JOB DESCRIPTION:\n" + jobDescription + "\n\n" +

                        "Return ONLY valid JSON in EXACTLY this format:\n" +
                        "{\n" +
                        "  \"match_score\": \"XX%\",\n" +
                        "  \"missing_keywords\": [\"keyword1\", \"keyword2\"],\n" +
                        "  \"suggestions\": \"Explain briefly how to improve the resume to match this job.\"\n" +
                        "}\n\n" +

                        "If the job description is unclear, too generic, or irrelevant → missing_keywords should explain that meaningful keywords are missing, and match_score MUST be very low (10–20%).";


        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        return webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {

                    List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                    if (candidates == null || candidates.isEmpty()) {
                        return new MatchResponseDTO("0%", List.of(), "No AI response");
                    }

                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    String rawOutput = parts.get(0).get("text").toString();

                    System.out.println("🤖 RAW Gemini OUTPUT:");
                    System.out.println(rawOutput);


                    String jsonString = extractJson(rawOutput);
                    System.out.println("📦 Extracted JSON:");
                    System.out.println(jsonString);

                    try {
                        return mapper.readValue(jsonString, MatchResponseDTO.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return new MatchResponseDTO("0%", List.of(), "Failed to parse AI response");
                    }
                })
                .timeout(Duration.ofSeconds(30))
                .retryWhen(Retry.backoff(1, Duration.ofSeconds(3)))
                .onErrorReturn(new MatchResponseDTO("0%", List.of(), "AI request failed (network issue)"));
    }

    private String extractJson(String text) {
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");

        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1).trim();
        }
        return "{}";
    }
}
