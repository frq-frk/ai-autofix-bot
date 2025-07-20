package com.saiyans.autofix.ai_review_bot.dto;

public class GitHubPullRequest {
	private int number;
	private String title;
	private String body;
	private User user;

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public static class User {
		private String login;

		public String getLogin() {
			return login;
		}

		public void setLogin(String login) {
			this.login = login;
		}

	}
}
