package com.example.springairag.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

	private final ChatClient chatClient;

	private final VectorStore vectorStore;

	public ChatController(ChatClient chatClient, VectorStore vectorStore) {
		this.chatClient = chatClient;
		this.vectorStore = vectorStore;
	}

	public record ChatRequest(String question) {
	}

	public record ChatResponse(String answer) {
	}

	@PostMapping("/api/chat")
	public ChatResponse ask(@RequestBody ChatRequest request) {
		String answer = chatClient.prompt()
			.advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
			.user(request.question())
			.call()
			.content();
		return new ChatResponse(answer);
	}

}
