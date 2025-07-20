# 🤖 AI Code Review Bot

A lightweight and extensible Spring Boot service that reviews Git diff patches using OpenAI models, applies clean code principles, and suggests intelligent improvements line-by-line.

---

## 🚀 Features

- 🔍 **Diff Analysis**: Accepts Git diff patches and understands code-level changes.
- 🧠 **AI-Powered Review**: Uses OpenAI to suggest improved code using clean code, formatting, naming, and logic best practices.
- ✍️ **Custom Diff Format**: Uses a minimal response format:
- 🧪 **Patch Application**: Replaces lines in the original source using the suggested diff.
- ✅ **Validations**: Ensures count of `[-]` and `[+]` lines are equal and applies changes reliably.
- ⚙️ **REST API Ready**: Easily integrate with your CI/CD pipelines or custom tools.

---

## 📦 API Overview
- API is not exposed directly to the clients but instead is used in a github app as web hook url.
- So to use the app install the github app provide access to the repo to the app.

## [GITHUB APP URL](https://github.com/settings/apps/saiyasn-autofix-ai-code-review-bot)

