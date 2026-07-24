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

**Ask a question (RAG)**

```bash
curl -X POST localhost:8080/api/chat \
  -H 'Content-Type: application/json' \
  -d '{"question": "What is Spring AI?"}'
```

The `/api/chat` endpoint uses `QuestionAnswerAdvisor` to retrieve relevant chunks
from Chroma and augment the prompt sent to the Ollama chat model.

## Build

```bash
./gradlew build
```
