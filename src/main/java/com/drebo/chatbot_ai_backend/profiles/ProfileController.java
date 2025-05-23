package com.drebo.chatbot_ai_backend.profiles;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileController {

    private final ProfileRepo profileRepo;

    public ProfileController(ProfileRepo profileRepo) {
        this.profileRepo = profileRepo;
    }

    //bypass cors check
    @CrossOrigin(origins = "*")
    @GetMapping("/profiles/random")
    public Profile getRandomProfile(){
        return profileRepo.getRandomProfile();
    }
}
