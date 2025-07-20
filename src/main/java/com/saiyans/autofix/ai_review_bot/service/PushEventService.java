package com.saiyans.autofix.ai_review_bot.service;

import com.saiyans.autofix.ai_review_bot.dto.ChangedFile;
import com.saiyans.autofix.ai_review_bot.model.PushEvent;
import com.saiyans.autofix.ai_review_bot.model.PushFile;
import com.saiyans.autofix.ai_review_bot.repository.PushEventRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PushEventService {
    private static final Logger log = LoggerFactory.getLogger(PushEventService.class);

    private final GitHubApiService githubApiService;
    private final PushEventRepository pushEventRepository;

    @Value("${github.app.bot.name}")
    private String botUsername;
    
    public PushEventService(GitHubApiService githubApiService, PushEventRepository pushEventRepository) {
        this.githubApiService = githubApiService;
        this.pushEventRepository = pushEventRepository;
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public void processWebhook(Map<String, Object> payload, String eventType) {
        if (!"push".equals(eventType)) return;

        try {
            Map<String, Object> repository = (Map<String, Object>) payload.get("repository");
            Map<String, Object> installation = (Map<String, Object>) payload.get("installation");

            String repoOwner = ((Map<String, Object>) repository.get("owner")).get("name").toString();
            String repoName = repository.get("name").toString();
            String branchRef = payload.get("ref").toString(); // e.g., refs/heads/main
            String branchName = branchRef.substring(branchRef.lastIndexOf("/") + 1);
            String afterCommitSha = payload.get("after").toString();
            long installationId = ((Number) installation.get("id")).longValue();

//            Check if branch name is AI bot created
            if(branchName.startsWith("ai-fix")) {
            	log.info("Skipping push event because it was made made by the ai auto fix bot :");
                return;
            }
            
         // ✅ Check if commit is from bot itself
            List<Map<String, Object>> commits = (List<Map<String, Object>>) payload.get("commits");
            for (Map<String, Object> commit : commits) {
                Map<String, Object> author = (Map<String, Object>) commit.get("author");
                String commitAuthorUsername = author.get("username") != null ? author.get("username").toString() : "";

                if (commitAuthorUsername.equalsIgnoreCase(botUsername)) {
                    log.info("Skipping push event because it was made by the bot user: {}", botUsername);
                    return;
                }
            }
            
            // Get GitHub App installation token
            String accessToken = githubApiService.getInstallationToken(installationId);

            // Fetch changed files from GitHub API
            List<ChangedFile> changedFiles = githubApiService.fetchCommitDiff(repoOwner, repoName, afterCommitSha, accessToken);

            // Create PushEvent entity
            PushEvent pushEvent = new PushEvent();
            pushEvent.setRepoOwner(repoOwner);
            pushEvent.setRepoName(repoName);
            pushEvent.setBranch(branchName);
            pushEvent.setCommitSha(afterCommitSha);
            pushEvent.setInstallationId(installationId);
            pushEvent.setAccessToken(accessToken);

            for (ChangedFile file : changedFiles) {
                PushFile pushFile = new PushFile();
                pushFile.setFilename(file.getFilename());
                pushFile.setPatch(file.getPatch());
                pushFile.setPushEvent(pushEvent);
                pushEvent.getFiles().add(pushFile);
            }

            pushEventRepository.save(pushEvent);
            log.info("Push event stored successfully for repo: {}/{} @ {}", repoOwner, repoName, afterCommitSha);

        } catch (Exception e) {
            log.error("Error processing push event webhook", e);
        }
    }
}