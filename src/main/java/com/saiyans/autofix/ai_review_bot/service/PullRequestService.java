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
public class PullRequestService {

    private static final Logger log = LoggerFactory.getLogger(PullRequestService.class);

    private final GitHubApiService githubApiService;
    private final PullRequestRepository pullRequestRepository;

    public PullRequestService(GitHubApiService githubApiService, PullRequestRepository pullRequestRepository) {
        this.githubApiService = githubApiService;
        this.pullRequestRepository = pullRequestRepository;
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public void processWebhook(Map<String, Object> payload, String eventType) {
        if (!"pull_request".equals(eventType)) return;

        String action = (String) payload.get("action");
        if (!"opened".equals(action) && !"synchronize".equals(action)) {
            log.info("PR action is '{}'; skipping.", action);
            return;
        }

        Map<String, Object> prMap = (Map<String, Object>) payload.get("pull_request");
        Map<String, Object> repoMap = (Map<String, Object>) payload.get("repository");
        Map<String, Object> installation = (Map<String, Object>) payload.get("installation");

        String repoOwner = ((Map<String, Object>) repoMap.get("owner")).get("login").toString();
        String repoName = repoMap.get("name").toString();
        int prNumber = ((Number) prMap.get("number")).intValue();
        long installationId = ((Number) installation.get("id")).longValue();

        // Get token
        String accessToken = githubApiService.getInstallationToken(installationId);

        // Fetch PR and file data
        GitHubPullRequest prData = githubApiService.fetchPullRequest(repoOwner, repoName, prNumber, accessToken);
        List<ChangedFile> changedFiles = githubApiService.fetchChangedFiles(repoOwner, repoName, prNumber, accessToken);

        // Save in DB
        PullRequest pr = new PullRequest();
        pr.setPrNumber((long) prData.getNumber());
        pr.setRepoOwner(repoOwner);
        pr.setRepoName(repoName);
        pr.setTitle(prData.getTitle());
        pr.setBody(prData.getBody());
        pr.setAuthor(prData.getUser().getLogin());
        pr.setReviewStatus(ReviewStatus.PENDING);
        pr.setInstallationId(installationId);
        pr.setAccessToken(accessToken);

        for (ChangedFile file : changedFiles) {
            PullRequestFile fileEntity = new PullRequestFile();
            fileEntity.setFilename(file.getFilename());
            fileEntity.setPatch(file.getPatch());
            fileEntity.setPullRequest(pr);
            pr.getFiles().add(fileEntity);
        }

        pullRequestRepository.save(pr);

        // 🔧 Step 5 (future): Apply analysis and patch (optional)
        // codeFixService.analyzeAndApplyFix(pr);

        // 🔀 Step 6: Create child branch and push changes
        // githubApiService.createBranch(repoOwner, repoName, accessToken, baseBranch, childBranch);
        // githubApiService.commitFileChanges(repoOwner, repoName, childBranch, modifiedFiles, accessToken);

        // 🚀 Step 7: Raise PR
        // githubApiService.createPullRequest(repoOwner, repoName, childBranch, baseBranch, "Fixes", "Auto-applied fixes", accessToken);
    }
}
