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

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
public class ProfileGeneratorService {

    private final ProfileRepo profileRepo;

    private static final String STABLE_DIFFUSION_API = "https://fe97a6a77c5448ab52.gradio.live/sdapi/v1/txt2img";

    private final OllamaChatModel ollamaChatModel;

    private final HttpClient httpClient;

    //create request builder for less repetition
    private final HttpRequest.Builder stableDiffusionRequestBuilder;

    private final List<Profile> generatedProfilesList = new ArrayList<>();

    private static final String PROFILES_FILE_PATH = "profiles.json";

    @Value("${startup-actions.initializeProfiles}")
    private Boolean initializeProfiles;

    @Value("${application.genderChoice}")
    private String genderChoice;

    @Value("${application.user}")
    private Map<String, String> userProfileProperties;

    @Value("${stablediffusion.api}")
    private String stableDiffusionApi;

    public ProfileGeneratorService(ProfileRepo profileRepo, OllamaChatModel ollamaChatModel){
        this.profileRepo = profileRepo;
        this.ollamaChatModel = ollamaChatModel;
        this.httpClient = HttpClient.newHttpClient();
        this.stableDiffusionRequestBuilder = HttpRequest.newBuilder()
                .setHeader("Content-type", "application/json")
                .uri(URI.create(stableDiffusionApi))
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
                            Generate a dating app profile and no other commentary:
                            Age: %d,
                            Ethnicity: %s,
                            Gender: %s,
                            Myers-Briggs Personality Type: %s,
                            Bio: ,
                            Save with saveProfile() function.
                            """
                    .formatted(age, ethnicity, gender, mbt);
            log.info(prompt);

            //call ollama to generate profile
            ChatResponse response = ollamaChatModel.call(new Prompt(prompt,
                    OllamaOptions.builder().withFunction("saveProfile").build()));
            log.info(response.getResult().getOutput().getContent());
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
            List<Profile> profilesWithImages = new ArrayList<>();
            for(Profile profile : generatedProfilesList){
                if(profile.imageUrl() == null){
                    profile = generateImage(profile);
                    profilesWithImages.add(profile);
                }
            }

            //convert profiles to json
            String jsonProfiles = new Gson().toJson(profilesWithImages);
            //open file -> write content -> close file
            FileWriter fileWriter = new FileWriter(PROFILES_FILE_PATH);
            fileWriter.write(jsonProfiles);
            fileWriter.close();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private Profile generateImage(Profile profile) {

        //uuid will be same as image url
        String uuid = profile.id().isBlank() ? UUID.randomUUID().toString() : profile.id();
        profile = new Profile(
                uuid,
                profile.firstName(),
                profile.lastName(),
                profile.age(),
                profile.ethnicity(),
                profile.gender(),
                profile.bio(),
                uuid + ".jpg",
                profile.personalityType()

        );

        String randomSelfie = Utils.randomSelfieTypes();

        //get request details
        String prompt = """
                Generate a dating app profile image:
                Age: %d
                Ethnicity: %s,
                Gender: %s.
                Myers-Briggs Personality Type: %s
                Bio: %s
                Image: Ultra-realistic, 4k DSLR, best quality, %s
                """
                .formatted(profile.age(), profile.ethnicity(), profile.gender().toString(), profile.personalityType().toString(), profile.bio(), randomSelfie);

        String negativePrompt = "Low-res, text, error, cropped, bad quality, low quality, jpeg artifacts, ugly, unattractive, deformed";
        String jsonRequest = """
                { "prompt": %s, "negative_prompt": %s, "steps": 2 }
                """
                .formatted(prompt, negativePrompt);

        //make post request to stable diffusion
        HttpRequest httpRequest = this.stableDiffusionRequestBuilder.POST(
                HttpRequest.BodyPublishers.ofString(jsonRequest)).build();

        HttpResponse<String> response;
        try {
            //image response
            response = this.httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        //convert image.json -> image instance
        record ImageResponse(List<String> images){}

        Gson gson = new Gson();
        ImageResponse imageResponse = gson.fromJson(response.body(), ImageResponse.class);
        if(imageResponse.images() != null && imageResponse.images().isEmpty()){
            String base64Image = imageResponse.images().getFirst();

            //Decode Base64 to binary
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            //define directory path
            String directoryPath = "resources/static/images/";
            //create full file path
            String filePath = directoryPath + profile.imageUrl();
            //check if directory exists
            Path directory = Paths.get(directoryPath);
            if(!Files.exists(directory)){
                try{
                    Files.createDirectories(directory);
                } catch (IOException e){
                    throw new RuntimeException(e);
                }
            }

            //save image to file
            try(FileOutputStream imageOutputFile = new FileOutputStream(filePath)){
                imageOutputFile.write(imageBytes);
            }catch (IOException e){
                return null;
            }
        }
        return profile;
    }

    @Bean
    @Description("Save AI generated profiles")
    public Function<Profile, Boolean> saveProfile(){
        return profile -> {
            log.info("function call");
            log.info(String.valueOf(profile));
            this.generatedProfilesList.add(profile);
            return true;
        };
    }

    public void saveGeneratedProfilesToDb(){
        Gson gson = new Gson();
        try {
            List<Profile> existingProfiles = gson.fromJson(
                    new FileReader(PROFILES_FILE_PATH),
                    new TypeToken<ArrayList<Profile>>(){}.getType()
            );
            profileRepo.deleteAll();
            profileRepo.saveAll(existingProfiles);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        Profile userProfile = new Profile(
                userProfileProperties.get("id"),
                userProfileProperties.get("firstName"),
                userProfileProperties.get("lastName"),
                Integer.parseInt(userProfileProperties.get("age")),
                userProfileProperties.get("ethnicity"),
                Gender.valueOf(userProfileProperties.get("gender")),
                userProfileProperties.get("bio"),
                userProfileProperties.get("imageUrl"),
                Mbt.valueOf(userProfileProperties.get("personalityType"))
        );
        System.out.println(userProfileProperties);
        profileRepo.save(userProfile);
    }
}
