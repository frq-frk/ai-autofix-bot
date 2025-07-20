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

### `POST /review/fix`

- **Description**: Sends a diff patch and gets suggested improvements from AI.
- **Request Body**:
```json
{
"patch": "Git-style diff string"
}
