package com.drebo.chatbot_ai_backend.coversations;

import com.drebo.chatbot_ai_backend.profiles.ProfileRepo;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.UUID;

@RestController
public class ConversationController {

    ConversationRepo conversationRepo;
    ProfileRepo profileRepo;

    public ConversationController(ConversationRepo conversationRepo, ProfileRepo profileRepo){
        this.conversationRepo = conversationRepo;
        this.profileRepo = profileRepo;
    }

    @PostMapping("/conversations")
    public Conversation createNewConversation(@RequestBody CreateConversationRequest request){

        profileRepo.findById(request.profileId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Conversation conversation = new Conversation(
                UUID.randomUUID().toString(),
                request.profileId(),
                new ArrayList<>()
        );

        conversationRepo.save(conversation);
        return conversation;
    }
}
