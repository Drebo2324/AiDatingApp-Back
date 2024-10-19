package com.drebo.chatbot_ai_backend.profiles;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Service
public class ProfileGeneratorService {

    private OllamaChatModel ollamaChatModel;

    private List<Profile> generatedProfilesList = new ArrayList<>();

    private static final String PROFILES_FILE_PATH = "profiles.json";

    public ProfileGeneratorService(OllamaChatModel ollamaChatModel){
        this.ollamaChatModel = ollamaChatModel;
    }

    public void generateProfile(int numberOfProfiles) throws InterruptedException {

        //find age range. genders, and ethnicities
        List<Integer> ages = new ArrayList<>(List.of(20, 30, 40, 50));
        List<Gender> genders = new ArrayList<>(List.of(Gender.MALE, Gender.FEMALE));
        List<String> ethnicities = new ArrayList<>(List.of("White", "Black", "Hispanic", "Asian"));
        Collections.shuffle(ages);
        Collections.shuffle(genders);
        Collections.shuffle(ethnicities);

        //for each combo of variables
        for(int age : ages) {
            for (Gender gender : genders) {
                for (String ethnicity : ethnicities) {
                    if(this.generatedProfilesList.size() >= numberOfProfiles) {
                        //save in json file
                        saveProfilesToJson(this.generatedProfilesList);
                        log.info("saved to json");

                        return;
                    }
                    String prompt = """
                            Create a dating app profile for a fictional person that includes a first and last name, 
                            age: %d, ethnicity: %s, gender: %s, a Mbt (Myers Briggs Personality Type), and a brief bio. 
                            Save in saveProfile() function.
                            """
                            .formatted(age, ethnicity, gender);

                    System.out.println(prompt);

                    //call ollama to generate profile
                    ChatResponse response = ollamaChatModel.call(new Prompt(prompt,
                            OllamaOptions.builder().withFunction("saveProfile").build()));

                    System.out.println(response.getResult().getOutput().getContent());

                    Thread.sleep(2000);
                }
            }
        };


        //open json file and read contents into a Set<Profiles>
        //save to mongodb
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
    @Description("Save generated profiles")
    public Function<Profile, Boolean> saveProfile(){
        return profile -> {
            System.out.println("function call");
            System.out.println(profile);
            this.generatedProfilesList.add(profile);
            return true;
        };
    }

}
