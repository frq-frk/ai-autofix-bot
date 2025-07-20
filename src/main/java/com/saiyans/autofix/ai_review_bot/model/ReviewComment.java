package com.saiyans.autofix.ai_review_bot.model;

import jakarta.persistence.*;

@Entity
public class ReviewComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @ManyToOne
    private PullRequestFile file;

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public PullRequestFile getFile() {
        return file;
    }

    public void setFile(PullRequestFile file) {
        this.file = file;
    }
}
