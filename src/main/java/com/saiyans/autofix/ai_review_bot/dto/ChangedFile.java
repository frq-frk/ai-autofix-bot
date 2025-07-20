package com.saiyans.autofix.ai_review_bot.dto;

import lombok.Data;

@Data
public class ChangedFile {
    private String filename;
    private String status;
    private String patch; // This is the diff
    
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getPatch() {
		return patch;
	}
	public void setPatch(String patch) {
		this.patch = patch;
	}
    
}
