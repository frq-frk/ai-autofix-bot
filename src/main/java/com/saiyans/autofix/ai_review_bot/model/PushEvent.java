package com.saiyans.autofix.ai_review_bot.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class PushEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String repoOwner;
    private String repoName;
    private String branch;
    private String commitSha;
    private String pusher;
    private Long installationId;

    @Lob
    @Column(length = 100000)
    private String accessToken;

    @OneToMany(mappedBy = "pushEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PushFile> files = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    private ReviewStatus reviewStatus = ReviewStatus.PENDING;

    public ReviewStatus getReviewStatus() {
		return reviewStatus;
	}

	public void setReviewStatus(ReviewStatus reviewStatus) {
		this.reviewStatus = reviewStatus;
	}

	public void addFile(PushFile file) {
        this.files.add(file);
        file.setPushEvent(this);
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRepoOwner() {
        return repoOwner;
    }

    public void setRepoOwner(String repoOwner) {
        this.repoOwner = repoOwner;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getCommitSha() {
        return commitSha;
    }

    public void setCommitSha(String commitSha) {
        this.commitSha = commitSha;
    }

    public String getPusher() {
        return pusher;
    }

    public void setPusher(String pusher) {
        this.pusher = pusher;
    }

    public Long getInstallationId() {
        return installationId;
    }

    public void setInstallationId(Long installationId) {
        this.installationId = installationId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public List<PushFile> getFiles() {
        return files;
    }

    public void setFiles(List<PushFile> files) {
        this.files = files;
    }
}