package com.saiyans.autofix.ai_review_bot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saiyans.autofix.ai_review_bot.model.PushEvent;
import com.saiyans.autofix.ai_review_bot.model.PushFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Service
public class GitHubService {
	
	Logger log = LoggerFactory.getLogger(GitHubService.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${github.app.bot.name}")
    private String botUsername;

    public GitHubService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.github.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.objectMapper = objectMapper;
    }

    public void createFixOnNewBranchAndRaisePR(PushEvent event, PushFile file, String suggestedFix) {
        String newBranchName = generateNewBranchName(file.getFilename());

        String latestCommitSha = fetchLatestCommitSha(event);
        createBranch(event, newBranchName, latestCommitSha);

        JsonNode fileContentJson = fetchFileContent(event, file.getFilename(), event.getBranch());
        String originalContent = extractDecodedContent(fileContentJson);
        String fileSha = fileContentJson.get("sha").asText();

        String updatedContent = applySuggestedFix(originalContent, suggestedFix);

        commitFix(event, file.getFilename(), updatedContent, fileSha, newBranchName);
        createPullRequest(event, file.getFilename(), newBranchName);
    }

    private String generateNewBranchName(String filename) {
        return "ai-fix-" + filename.replace("/", "_") + "-" + Instant.now().toEpochMilli();
    }

    private String fetchLatestCommitSha(PushEvent event) {
        String url = "/repos/" + event.getRepoOwner() + "/" + event.getRepoName() + "/git/ref/heads/" + event.getBranch();

        JsonNode response = webClient.get()
                .uri(url)
                .headers(headers -> headers.setBearerAuth(event.getAccessToken()))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        return response.get("object").get("sha").asText();
    }

    private void createBranch(PushEvent event, String newBranchName, String sha) {
        String url = "/repos/" + event.getRepoOwner() + "/" + event.getRepoName() + "/git/refs";

        String payload = String.format("{\"ref\": \"refs/heads/%s\", \"sha\": \"%s\"}", newBranchName, sha);

        webClient.post()
                .uri(url)
                .headers(headers -> headers.setBearerAuth(event.getAccessToken()))
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private JsonNode fetchFileContent(PushEvent event, String filePath, String branch) {
        String url = "/repos/" + event.getRepoOwner() + "/" + event.getRepoName() + "/contents/" + filePath + "?ref=" + branch;

        return webClient.get()
                .uri(url)
                .headers(headers -> headers.setBearerAuth(event.getAccessToken()))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    private String extractDecodedContent(JsonNode contentJson) {
        String encodedContent = contentJson.get("content").asText().replaceAll("\\n", "");
        byte[] decodedBytes = Base64.getDecoder().decode(encodedContent);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    private String applySuggestedFix(String originalContent, String suggestedFix) {
        List<String> lines = Arrays.asList(suggestedFix.split("\n"));
        String updatedContent = originalContent;

        String currentRemoved = null;
        List<String> currentAdded = new ArrayList<>();

        for (String rawLine : lines) {
            String line = rawLine.trim();

            if (line.startsWith("[-]")) {
                // Process previous group if exists
                if (currentRemoved != null) {
                    updatedContent = replaceLine(updatedContent, currentRemoved, currentAdded);
                    currentAdded.clear();
                }

                currentRemoved = line.substring(4).trim();
            } else if (line.startsWith("[+]")) {
                currentAdded.add(line.substring(4).trim());
            }
        }

        // Process last group
        if (currentRemoved != null) {
            updatedContent = replaceLine(updatedContent, currentRemoved, currentAdded);
        }

        return updatedContent;
    }

    private String replaceLine(String content, String toRemove, List<String> toAdd) {
        if (content.contains(toRemove)) {
            String replacement = String.join("\n", toAdd);
            return content.replace(toRemove, replacement);
        } else {
            log.info("Line not found in content: " + toRemove);
            return content; // or optionally append a comment about failure
        }
    }

    private void commitFix(PushEvent event, String filePath, String updatedContent, String sha, String branch) {
        String url = "/repos/" + event.getRepoOwner() + "/" + event.getRepoName() + "/contents/" + filePath;

        String encodedContent = Base64.getEncoder().encodeToString(updatedContent.getBytes(StandardCharsets.UTF_8));

        String payload = String.format("""
                {
                    "message": "chore(ai-fix): Apply AI suggested fix to %s",
                    "content": "%s",
                    "branch": "%s",
                    "sha": "%s",
                    "committer": {
                        "name": "%s",
                        "email": "%s@users.noreply.github.com"
                    },
                    "author": {
                        "name": "%s",
                        "email": "%s@users.noreply.github.com"
                    }
                }
                """, filePath, encodedContent, branch, sha, botUsername, botUsername, botUsername, botUsername);

        webClient.put()
                .uri(url)
                .headers(headers -> headers.setBearerAuth(event.getAccessToken()))
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private void createPullRequest(PushEvent event, String filePath, String headBranch) {
        String url = "/repos/" + event.getRepoOwner() + "/" + event.getRepoName() + "/pulls";

        String payload = String.format("""
                {
                    "title": "AI Suggested Fix for %s",
                    "head": "%s",
                    "base": "%s",
                    "body": "This PR contains an AI-generated fix for the file `%s`."
                }
                """, filePath, headBranch, event.getBranch(), filePath);

        webClient.post()
                .uri(url)
                .headers(headers -> headers.setBearerAuth(event.getAccessToken()))
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
