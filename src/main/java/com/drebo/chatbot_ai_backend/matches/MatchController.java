package com.drebo.chatbot_ai_backend.matches;

import com.drebo.chatbot_ai_backend.coversations.Conversation;
import com.drebo.chatbot_ai_backend.coversations.ConversationRepo;
import com.drebo.chatbot_ai_backend.profiles.Profile;
import com.drebo.chatbot_ai_backend.profiles.ProfileRepo;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
public class MatchController {

    private final MatchRepo matchRepo;
    private final ProfileRepo profileRepo;
    private final ConversationRepo conversationRepo;

    public MatchController(MatchRepo matchRepo, ProfileRepo profileRepo, ConversationRepo conversationRepo) {
        this.matchRepo = matchRepo;
        this.profileRepo = profileRepo;
        this.conversationRepo = conversationRepo;
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/matches")
    public Match createNewMatch(@RequestBody CreateMatchRequest request){

        Profile profile = profileRepo.findById(request.profileId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile Id not found" + request.profileId()));

        //TODO: VALIDATE NO EXISTING CONVO WITH THIS PROFILE
        Conversation conversation = new Conversation(
                UUID.randomUUID().toString(),
                request.profileId(),
                new ArrayList<>()
        );

        Conversation savedConversation = conversationRepo.save(conversation);
        Match match = new Match(UUID.randomUUID().toString(), profile, conversation.id());
        Match savedMatch = matchRepo.save(match);
        return savedMatch;
    }

    @GetMapping("/matches")
    public List<Match> getAllMatches(){
        return matchRepo.findAll();
    }
}
