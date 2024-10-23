package com.drebo.chatbot_ai_backend.profiles;

import com.drebo.chatbot_ai_backend.Utils;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Service
public class ProfileGeneratorService {

    private final OllamaChatModel ollamaChatModel;

    private final List<Profile> generatedProfilesList = new ArrayList<>();

    private static final String PROFILES_FILE_PATH = "profiles.json";

    @Value("${startup-actions.initializeProfiles}")
    private Boolean initializeProfiles;

    @Value("${application.genderChoice}")
    private String genderChoice;

    public ProfileGeneratorService(OllamaChatModel ollamaChatModel){
        this.ollamaChatModel = ollamaChatModel;
    }

    public void generateProfile(int numberOfProfiles) throws InterruptedException {

        if(!this.initializeProfiles){
            return;
        }

        while(this.generatedProfilesList.size() < numberOfProfiles){

            int age = Utils.randomNumber(20, 30);
            String gender = genderChoice;
            String ethnicity = Utils.randomEthnicity();
            String mbt = Utils.randomMbt().toString();

            String prompt = """
                            Generate a dating app profile that must include first name, last name,
                            age: %d, ethnicity: %s, gender: %s, a Myers Briggs Personality Type: %s, and a bio.
                            Save in saveProfile() function.
                            Only generate the profile and no other commentary.
                            """
                    .formatted(age, ethnicity, gender, mbt);
            System.out.println(prompt);

            //call ollama to generate profile
            ChatResponse response = ollamaChatModel.call(new Prompt(prompt,
                    OllamaOptions.builder().withFunction("saveProfile").build()));
            System.out.println(response.getResult().getOutput().getContent());
        }
        saveProfilesToJson(this.generatedProfilesList);
    }

    private void saveProfilesToJson(List<Profile> generatedProfilesList) {

        //convert profiles to json
        String jsonProfiles = new Gson().toJson(generatedProfilesList);

        //open file -> write content -> close file
        try {
            FileWriter fileWriter = new FileWriter(PROFILES_FILE_PATH);
            fileWriter.write(jsonProfiles);
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Bean
    @Description("Save AI generated profiles")
    public Function<Profile, Boolean> saveProfile(){
        return profile -> {
            System.out.println("function call");
            System.out.println(profile);
            this.generatedProfilesList.add(profile);
            return true;
        };
    }

}
