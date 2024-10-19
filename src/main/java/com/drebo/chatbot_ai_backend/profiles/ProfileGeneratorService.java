package com.drebo.chatbot_ai_backend.profiles;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Service
public class ProfileGeneratorService {

    private OllamaChatModel ollamaChatModel;

    private List<Profile> generatedProfilesList = new ArrayList<>();

    public ProfileGeneratorService(OllamaChatModel ollamaChatModel){
        this.ollamaChatModel = ollamaChatModel;
    }

    public void generateProfile(int numberOfProfiles){

        //find age range. genders, and ethnicities
        List<Integer> ages = new ArrayList<>(List.of(20, 30, 40, 50));
        List<Gender> genders = new ArrayList<>(List.of(Gender.MALE, Gender.FEMALE));
        List<String> ethnicities = new ArrayList<>(List.of("White", "Black", "Hispanic", "Asian"));
        Collections.shuffle(ages);
        Collections.shuffle(genders);
        Collections.shuffle(ethnicities);

        for(int age : ages) {
            for (Gender gender : genders) {
                for (String ethnicity : ethnicities) {
                    if(this.generatedProfilesList.size() >= numberOfProfiles) {
                        return;
                    }
                    String prompt = """
                            Generate a creative fictional profile that includes a FICTIONAL first and last name, 
                            age: %d, ethnicity: %s, gender: %s, and a Myers Briggs Personality type, 
                            and a brief bio that this person would write to display personality.
                            First name cannot be null, age cannot be null, gender cannot be null(GENDER.MALE, GENDER.FEMALE). 
                            """
                            .formatted(age, ethnicity, gender);

                    System.out.println(prompt);

                    ChatResponse response = ollamaChatModel.call(new Prompt(prompt,
                            OllamaOptions.builder().withFunction("saveProfile").build()));

                    System.out.println(response.getResult().getOutput().getContent());
                }
            }
        };



        //for each combo of variables
            //call ollama to generate profile
            //save in json file
        //open json file and read contents into a Set<Profiles>
        //save to mongodb
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
