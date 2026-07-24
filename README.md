# spring-ai-rag

A Retrieval-Augmented Generation (RAG) starter built with:

- **Spring Boot 4.1.0** / Spring Framework 7
- **Java 21**
- **Gradle** (via the included `gradlew` wrapper)
- **Spring AI 2.0.0**
  - [Ollama](https://ollama.com/) for local chat + embedding models
  - [Chroma](https://www.trychroma.com/) as the vector store
  - Markdown / PDF document readers for ingesting source material

## Prerequisites

- JDK 21
- Docker (to run Ollama and Chroma via `compose.yaml`, auto-started by Spring Boot's Docker Compose support when you run the app)

## Running

```bash
./gradlew bootRun
```

Then open **http://localhost:8080** in a browser for a small built-in web UI
(`src/main/resources/static/index.html`) — ingest text/Markdown/PDF on the left,
chat with RAG + conversation memory on the right. It's a static page calling the
same REST API described below, no separate frontend build required.

Spring Boot's Docker Compose integration will bring up `ollama` and `chroma`
(defined in `compose.yaml`) automatically. On first run, pull the models Ollama needs:

```bash
docker exec -it $(docker ps -qf ancestor=ollama/ollama) ollama pull llama3.2
docker exec -it $(docker ps -qf ancestor=ollama/ollama) ollama pull mxbai-embed-large
```

(Adjust the model names in `src/main/resources/application.yml` if you use different ones.)

## API

**Ingest raw text**

```bash
curl -X POST localhost:8080/api/documents/text \
  -H 'Content-Type: application/json' \
  -d '{"content": "Spring AI simplifies building AI applications with Spring.", "metadata": {"source": "manual"}}'
```

**Ingest a Markdown file**

```bash
curl -X POST localhost:8080/api/documents/markdown -F file=@notes.md
```

**Ingest a PDF file**

```bash
curl -X POST localhost:8080/api/documents/pdf -F file=@paper.pdf
```

**Ask a question (RAG, with conversation memory)**

```bash
curl -X POST localhost:8080/api/chat \
  -H 'Content-Type: application/json' \
  -d '{"question": "What is Spring AI?", "conversationId": "session-1"}'
```

The `/api/chat` endpoint combines two advisors:
- `QuestionAnswerAdvisor` retrieves relevant chunks from Chroma and augments the
  prompt sent to the Ollama chat model (RAG).
- `MessageChatMemoryAdvisor` keeps the last 20 messages per `conversationId` in an
  in-memory `ChatMemory`, so follow-up questions in the same conversation have
  context from earlier turns. Omit `conversationId` to use a shared `"default"`
  conversation, or pass a distinct id per user/session to keep histories separate.
  The memory store is in-process and resets on restart; swap in one of Spring AI's
  persistent `ChatMemoryRepository` implementations (JDBC, Redis, etc.) for
  production use.

**Clear a conversation**

```bash
curl -X DELETE localhost:8080/api/chat/session-1
```

## Build

```bash
./gradlew build
```
