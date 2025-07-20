package com.saiyans.autofix.ai_review_bot.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.saiyans.autofix.ai_review_bot.dto.ChangedFile;
import com.saiyans.autofix.ai_review_bot.dto.GitHubPullRequest;
import com.saiyans.autofix.ai_review_bot.model.PullRequest;
import com.saiyans.autofix.ai_review_bot.model.PullRequestFile;
import com.saiyans.autofix.ai_review_bot.model.ReviewStatus;
import com.saiyans.autofix.ai_review_bot.repository.PullRequestRepository;

import jakarta.transaction.Transactional;

@Service
public class WebhookService {
	private static final Logger log = LoggerFactory.getLogger(WebhookService.class);
	
	private final GitHubApiService githubApiService;
	private final PullRequestRepository pullRequestRepository;

    public WebhookService(GitHubApiService githubApiService, PullRequestRepository pullRequestRepository) {
        this.githubApiService = githubApiService;
        this.pullRequestRepository = pullRequestRepository;
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public void processWebhook(Map<String, Object> payload, String eventType) {
        if (!"pull_request".equals(eventType)) return;
        
        if (!payload.get("action").equals("opened") && !payload.get("action").equals("synchronize")) {
            log.info("exiting as the pr even it not opened!!");
        	return; // skip processing
        }

        // Extract info
        Map<String, Object> pullRequest = (Map<String, Object>) payload.get("pull_request");
        Map<String, Object> repo = (Map<String, Object>) payload.get("repository");
        Map<String, Object> installation = (Map<String, Object>) payload.get("installation");

        String repoOwner = ((Map<String, Object>) repo.get("owner")).get("login").toString();
        String repoName = repo.get("name").toString();
        int prNumber = ((Number) pullRequest.get("number")).intValue();
        long installationId = ((Number) installation.get("id")).longValue(); // 🆕 Get installation ID

        // Get installation token (to call GitHub APIs as the bot)
        String accessToken = githubApiService.getInstallationToken(installationId); // 🆕 Add this service

        GitHubPullRequest prData = githubApiService.fetchPullRequest(repoOwner, repoName, prNumber, accessToken);
        List<ChangedFile> changedFiles = githubApiService.fetchChangedFiles(repoOwner, repoName, prNumber, accessToken);

        PullRequest pr = new PullRequest();
        pr.setPrNumber((long) prData.getNumber());
        pr.setRepoOwner(repoOwner);
        pr.setRepoName(repoName);
        pr.setTitle(prData.getTitle());
        pr.setBody(prData.getBody());
        pr.setAuthor(prData.getUser().getLogin());
        pr.setReviewStatus(ReviewStatus.PENDING);
        pr.setInstallationId(installationId); // 🆕 Save this to use later
        pr.setAccessToken(accessToken);       // 🆕 Optionally persist encrypted token for scheduler

        for (ChangedFile file : changedFiles) {
            PullRequestFile fileEntity = new PullRequestFile();
            fileEntity.setFilename(file.getFilename());
            fileEntity.setPatch(file.getPatch());
            fileEntity.setPullRequest(pr);
            pr.getFiles().add(fileEntity);
        }

        pullRequestRepository.save(pr);
    }
}
