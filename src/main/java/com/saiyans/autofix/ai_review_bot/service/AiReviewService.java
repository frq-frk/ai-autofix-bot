package com.saiyans.autofix.ai_review_bot.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AiReviewService {

    private static final Logger log = LoggerFactory.getLogger(AiReviewService.class);
    
    private static final Logger llmLogger = LoggerFactory.getLogger("LLM_LOGGER");

    private final WebClient webClient;

    public AiReviewService(@Value("${openai.api.key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public String reviewAndFixPatch(String patch) {
        String prompt = buildFixPrompt(patch);

        Map<String, Object> request = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a senior software engineer. Fix the patch with best practices."),
                        Map.of("role", "user", "content", prompt)
                )
        );
        llmLogger.info("=== LLM Request ===\n" + request);
        try {
            return webClient.post()
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(15))
                    .retry(3)
                    .map(resp -> {
                        List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.get("choices");
                        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                        llmLogger.info("=== LLM response ===\n" + message);
                        return (String) message.get("content");
                    })
                    .block();
        } catch (Exception e) {
            log.error("Error while calling OpenAI API", e);
            return "";
        }
    }

    private String buildFixPrompt(String patch) {
        return String.format("""
            You are a senior software engineer tasked with improving a Git patch.

            Instructions:
            - Carefully review the following Git-style diff and suggest only **actual code improvements**.
            - Return ONLY the changed lines using this exact format:
                [-] original line
                [+] improved line
            - The number of [-] and [+] lines MUST be equal.
            - DO NOT include unchanged lines.
            - DO NOT include anything else — no commentary, no markdown, no ```diff blocks.
            - DO NOT repeat input lines unless they are improved.
            - If nothing can be improved, return an empty response.

            Input patch:
            %s
            """, patch);
    }

}