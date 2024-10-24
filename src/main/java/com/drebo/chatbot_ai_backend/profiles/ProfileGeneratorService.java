package com.drebo.chatbot_ai_backend.profiles;

import com.drebo.chatbot_ai_backend.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Service
public class ProfileGeneratorService {

    private static final String STABLE_DIFFUSION_API = "null";

    private final OllamaChatModel ollamaChatModel;

    private HttpClient httpClient;

    //create request builder for less repetition
    private HttpRequest.Builder stableDiffusionRequestBuilder;

    private final List<Profile> generatedProfilesList = new ArrayList<>();

    private static final String PROFILES_FILE_PATH = "profiles.json";

    @Value("${startup-actions.initializeProfiles}")
    private Boolean initializeProfiles;

    @Value("${application.genderChoice}")
    private String genderChoice;

    public ProfileGeneratorService(OllamaChatModel ollamaChatModel){
        this.ollamaChatModel = ollamaChatModel;
        this.httpClient = HttpClient.newHttpClient();
        this.stableDiffusionRequestBuilder = HttpRequest.newBuilder()
                .setHeader("Content-type", "application/json")
                .uri(URI.create(STABLE_DIFFUSION_API))
        ;
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

        try {
            Gson gson = new Gson();
            //add existing profiles
            List<Profile> existingProfiles = gson.fromJson(
                    new FileReader(PROFILES_FILE_PATH),
                    //deserialize json into arraylist of profiles
                    new TypeToken<ArrayList<Profile>>(){}.getType()
            );
            generatedProfilesList.addAll(existingProfiles);

            //call stable diffusion for profiles with no image
            for(Profile profile : generatedProfilesList){
                if(profile.imageUrl() == null){
                    profile = generateImage(profile);
                }
            }

            //convert profiles to json
            String jsonProfiles = new Gson().toJson(generatedProfilesList);
            //open file -> write content -> close file
            FileWriter fileWriter = new FileWriter(PROFILES_FILE_PATH);
            fileWriter.write(jsonProfiles);
            fileWriter.close();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private Profile generateImage(Profile profile) {

        //get request details
        String prompt = """
                Generate a dating app profile image of a %d
                years old, %s, %s.
                Personality: %s
                Bio: %s
                Ultra-realistic, 4k DSLR, best quality
                """
                .formatted(profile.age(), profile.ethnicity(), profile.gender(), profile.personalityType(), profile.bio());

        String negativePrompt = "Low-res, text, error, cropped, bad quality, low quality, jpeg artifacts, ugly, unattractive, deformed";
        String jsonRequest = """
                { "prompt": %s, "negative_prompt": %s
                }
                """
                .formatted(prompt, negativePrompt);

        //make post request
        HttpRequest httpRequest = this.stableDiffusionRequestBuilder.POST(
                HttpRequest.BodyPublishers.ofString(jsonRequest)).build();

        HttpResponse<String> response;
        try {
            //image
            response = this.httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return profile;

        //save image to resource folder

        //image -> profile.imageUrl

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
