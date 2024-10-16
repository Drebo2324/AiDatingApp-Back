package com.drebo.chatbot_ai_backend;

import com.drebo.chatbot_ai_backend.coversations.ChatMessage;
import com.drebo.chatbot_ai_backend.coversations.Conversation;
import com.drebo.chatbot_ai_backend.coversations.ConversationRepo;
import com.drebo.chatbot_ai_backend.profiles.Gender;
import com.drebo.chatbot_ai_backend.profiles.Mbt;
import com.drebo.chatbot_ai_backend.profiles.Profile;
import com.drebo.chatbot_ai_backend.profiles.ProfileRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
public class ChatbotAiBackendApplication implements CommandLineRunner {

	@Autowired
	ProfileRepo profileRepo;

	@Autowired
	ConversationRepo conversationRepo;

	public static void main(String[] args) {
		SpringApplication.run(ChatbotAiBackendApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

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
				"boobies.jpg",
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
