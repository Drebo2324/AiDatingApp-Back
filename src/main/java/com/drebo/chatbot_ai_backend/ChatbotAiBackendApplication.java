package com.drebo.chatbot_ai_backend;

import com.drebo.chatbot_ai_backend.profiles.Gender;
import com.drebo.chatbot_ai_backend.profiles.Mbt;
import com.drebo.chatbot_ai_backend.profiles.Profile;
import com.drebo.chatbot_ai_backend.profiles.ProfileRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChatbotAiBackendApplication implements CommandLineRunner {

	@Autowired
	ProfileRepo profileRepo;

	public static void main(String[] args) {
		SpringApplication.run(ChatbotAiBackendApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Profile profile = new Profile(
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

		profileRepo.save(profile);
		profileRepo.findAll().forEach(System.out::println);
	}
}
