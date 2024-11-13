package com.drebo.chatbot_ai_backend.coversations;

import com.drebo.chatbot_ai_backend.profiles.Profile;
import com.drebo.chatbot_ai_backend.profiles.ProfileRepo;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@RestController
public class ConversationController {

    private final ConversationRepo conversationRepo;
    private final ProfileRepo profileRepo;
    private final ConversationService conversationService;

    public ConversationController(ConversationRepo conversationRepo, ProfileRepo profileRepo, ConversationService conversationService){
        this.conversationRepo = conversationRepo;
        this.profileRepo = profileRepo;
        this.conversationService = conversationService;
    }

    @GetMapping("/conversations/{conversationId}")
    public Conversation getConversation(@PathVariable String conversationId){
        Conversation conversation = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation Id not found" + conversationId));

        return conversation;
    }

    @PostMapping("/conversations")
    public Conversation createNewConversation(@RequestBody CreateConversationRequest request){

        profileRepo.findById(request.profileId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile Id not found" + request.profileId()));

        Conversation conversation = new Conversation(
                UUID.randomUUID().toString(),
                request.profileId(),
                new ArrayList<>()
        );

        Conversation savedConversation = conversationRepo.save(conversation);
        return savedConversation;
    }

    @PostMapping("/conversations/{conversationId}")
    public Conversation sendMessage(@PathVariable String conversationId, @RequestBody ChatMessage chatMessage){

        Conversation conversation = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation Id not found" + conversationId));

        String matchProfileId = conversation.profileId();

        Profile matchProfile = profileRepo.findById(matchProfileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile Id not found" + matchProfileId));

        Profile user = profileRepo.findById(chatMessage.authorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile Id not found" + chatMessage.authorId()));

        //TODO: validate author of message is same as profile/user

        //don't trust user to add valid dateTime in request
        //transfer chatMessage data and add dateTime
        //Records immutable
        ChatMessage timedChatMessage= new ChatMessage(
                chatMessage.authorId(),
                chatMessage.text(),
                LocalDateTime.now()
        );

        //messages is an ArrayList so can be mutable despite Conversation being a record
        conversation.messages().add(timedChatMessage);
        conversationService.generateAiResponse(conversation, matchProfile, user);
        Conversation savedConversation = conversationRepo.save(conversation);
        return savedConversation;
    }
}
