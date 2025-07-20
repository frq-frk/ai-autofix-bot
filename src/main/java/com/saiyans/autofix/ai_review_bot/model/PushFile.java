package com.saiyans.autofix.ai_review_bot.model;

import jakarta.persistence.*;

@Entity
public class PushFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;

    @Lob
    @Column(length = 100000)
    private String patch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "push_event_id")
    private PushEvent pushEvent;
    
    // Getters and setters

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getPatch() {
		return patch;
	}

	public void setPatch(String patch) {
		this.patch = patch;
	}

	public PushEvent getPushEvent() {
		return pushEvent;
	}

	public void setPushEvent(PushEvent pushEvent) {
		this.pushEvent = pushEvent;
	}

}
