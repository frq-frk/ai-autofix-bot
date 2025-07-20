package com.saiyans.autofix.ai_review_bot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.saiyans.autofix.ai_review_bot.service.PushEventService;

import java.util.Map;

@RestController
@RequestMapping("/events")
public class WebhookController {
	
	private final PushEventService pushEventService;

    public WebhookController(PushEventService pushEventService) {
		super();
		this.pushEventService = pushEventService;
	}

	@PostMapping
    public ResponseEntity<String> handleGitHubEvent(
            @RequestHeader("X-GitHub-Event") String eventType,
            @RequestBody Map<String, Object> payload
    ) {
        System.out.println("📦 Received GitHub Event: " + eventType);
        pushEventService.processWebhook(payload, eventType);
        // TODO: Extract repo/branch/commit info and process further
        return ResponseEntity.ok("Event received");
    }
}

