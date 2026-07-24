package com.example.springairag.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

	private static final String DEFAULT_CONVERSATION_ID = "default";

	private final ChatClient chatClient;

	private final VectorStore vectorStore;

	private final ChatMemory chatMemory;

	public ChatController(ChatClient chatClient, VectorStore vectorStore, ChatMemory chatMemory) {
		this.chatClient = chatClient;
		this.vectorStore = vectorStore;
		this.chatMemory = chatMemory;
	}

	public record ChatRequest(String question, String conversationId) {
	}

	public record ChatResponse(String answer, String conversationId) {
	}

	@PostMapping("/api/chat")
	public ChatResponse ask(@RequestBody ChatRequest request) {
		String conversationId = StringUtils.hasText(request.conversationId()) ? request.conversationId()
				: DEFAULT_CONVERSATION_ID;

		String answer = chatClient.prompt()
			.advisors(MessageChatMemoryAdvisor.builder(chatMemory).build(),
					QuestionAnswerAdvisor.builder(vectorStore).build())
			.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
			.user(request.question())
			.call()
			.content();
		return new ChatResponse(answer, conversationId);
	}

	@DeleteMapping("/api/chat/{conversationId}")
	public ResponseEntity<Void> clear(@PathVariable String conversationId) {
		chatMemory.clear(conversationId);
		return ResponseEntity.ok().build();
	}

}
