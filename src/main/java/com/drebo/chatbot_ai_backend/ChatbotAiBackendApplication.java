package com.drebo.chatbot_ai_backend;

import com.drebo.chatbot_ai_backend.coversations.ChatMessage;
import com.drebo.chatbot_ai_backend.coversations.Conversation;
import com.drebo.chatbot_ai_backend.coversations.ConversationRepo;
import com.drebo.chatbot_ai_backend.profiles.*;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
public class ChatbotAiBackendApplication implements CommandLineRunner {

	@Autowired
	private ProfileRepo profileRepo;

	@Autowired
	private ConversationRepo conversationRepo;

	@Autowired
	private ProfileGeneratorService profileGeneratorService;

	public static void main(String[] args) {
		SpringApplication.run(ChatbotAiBackendApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

//		profileGeneratorService.generateProfile(3);
//
//		profileRepo.deleteAll();
//		conversationRepo.deleteAll();

	}
}
