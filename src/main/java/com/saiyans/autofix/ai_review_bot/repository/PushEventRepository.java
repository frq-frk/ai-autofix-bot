package com.saiyans.autofix.ai_review_bot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.saiyans.autofix.ai_review_bot.model.PushEvent;
import com.saiyans.autofix.ai_review_bot.model.ReviewStatus;

public interface PushEventRepository extends JpaRepository<PushEvent, Long> {

	List<PushEvent> findByReviewStatus(ReviewStatus pending);
}
