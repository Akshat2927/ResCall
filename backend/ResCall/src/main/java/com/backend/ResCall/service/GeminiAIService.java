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
                "You are an ATS evaluator.\n\n" +
                        "Compare the following:\n\n" +
                        "RESUME:\n" + resumeText + "\n\n" +
                        "JOB DESCRIPTION:\n" + jobDescription + "\n\n" +
                        "Return ONLY valid JSON in this format:\n" +
                        "{\n" +
                        "  \"match_score\": \"85%\",\n" +
                        "  \"missing_keywords\": [\"skill1\", \"skill2\"],\n" +
                        "  \"suggestions\": \"Improve by adding ...\"\n" +
                        "}";

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
