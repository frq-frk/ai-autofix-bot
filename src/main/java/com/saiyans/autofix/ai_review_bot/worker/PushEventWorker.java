package com.saiyans.autofix.ai_review_bot.worker;

import com.saiyans.autofix.ai_review_bot.model.PushEvent;
import com.saiyans.autofix.ai_review_bot.model.PushFile;
import com.saiyans.autofix.ai_review_bot.model.ReviewStatus;
import com.saiyans.autofix.ai_review_bot.repository.PushEventRepository;
import com.saiyans.autofix.ai_review_bot.service.AiReviewService;
import com.saiyans.autofix.ai_review_bot.service.GitHubService;

import jakarta.transaction.Transactional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PushEventWorker {

    private final PushEventRepository pushEventRepository;
    private final AiReviewService aiReviewService;
    private final GitHubService gitHubService;

    public PushEventWorker(
            PushEventRepository pushEventRepository,
            AiReviewService aiReviewService,
            GitHubService gitHubService
    ) {
        this.pushEventRepository = pushEventRepository;
        this.aiReviewService = aiReviewService;
        this.gitHubService = gitHubService;
    }

    // Runs every 30 seconds (adjust as needed)
    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void processPendingPushEvents() {
        List<PushEvent> pendingEvents = pushEventRepository.findByReviewStatus(ReviewStatus.PENDING);

        for (PushEvent event : pendingEvents) {
            try {
                System.out.println("Processing push event: " + event.getId());

                List<PushFile> files = event.getFiles();

                for (PushFile file : files) {
                    String patch = file.getPatch();
                    String filename = file.getFilename();

                    if (patch == null || patch.isBlank()) continue;

                    String suggestedFix = aiReviewService.reviewAndFixPatch(patch);

                    if (suggestedFix != null && !suggestedFix.isBlank()) {
                        gitHubService.createFixOnNewBranchAndRaisePR(
                                event,
                                file,
                                suggestedFix
                        );
                    }
                }

                event.setReviewStatus(ReviewStatus.DONE);
                pushEventRepository.save(event);

            } catch (Exception e) {
                event.setReviewStatus(ReviewStatus.FAILED);
                pushEventRepository.save(event);
                e.printStackTrace();
            }
        }
    }
}