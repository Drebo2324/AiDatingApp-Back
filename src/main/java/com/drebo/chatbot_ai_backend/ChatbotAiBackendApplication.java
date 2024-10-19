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

		profileGeneratorService.generateProfile(3);

		profileRepo.deleteAll();
		conversationRepo.deleteAll();

		Profile profile1 = new Profile(
				"1",
				"Dre",
				"Bo",
				69,
				"Martian",
				Gender.MALE,
				"Likes receiving head",
				"boobs.jpg",
				Mbt.ENFJ
		);

		Profile profile2 = new Profile(
				"2",
				"Andre",
				"Ju",
				80085,
				"Zebra",
				Gender.MALE,
				"I like boobs",
				"boobies.jpg",
				Mbt.ESFP
		);

		profileRepo.save(profile1);
		profileRepo.save(profile2);
		profileRepo.findAll().forEach(System.out::println);

		Conversation conversation = new Conversation(
				"1",
				profile1.id(),
				List.of(
						new ChatMessage(
								"Yoooo!!!", profile1.id(), LocalDateTime.now()

						)
				)
		);

		conversationRepo.save(conversation);
		conversationRepo.findAll().forEach(System.out::println);
	}
}
