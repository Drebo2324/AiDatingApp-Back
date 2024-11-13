package com.drebo.chatbot_ai_backend.coversations;

import com.drebo.chatbot_ai_backend.profiles.Profile;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConversationService {

    private final OllamaChatModel ollamaChatModel ;

    public ConversationService(OllamaChatModel ollamaChatModel){
        this.ollamaChatModel = ollamaChatModel;
    }

    public Conversation generateAiResponse(Conversation conversation, Profile matchProfile, Profile user){

        //SYSTEM -> SystemMessage
        //USER -> UserMessage
        //ASSISTANT -> AssistantMessage
        //TOOL -> ToolResponseMessage

        String systemMessagePrompt = """
                You are the following:
                Name: %s %s,
                Age: %d,
                Ethnicity: %s,
                Gender: %s,
                Myers-Briggs Personality Type: %s,
                Bio: %s
                
                You are having a conversation with a:
                Name: %s %s,
                Age: %d,
                Ethnicity: %s,
                Gender: %s,
                Myers-Briggs Personality Type: %s,
                Bio: %s
                
                This is an in-app text conversation. Play only the role you were given.
                """
                .formatted(
                        matchProfile.firstName(), matchProfile.lastName(), matchProfile.age(), matchProfile.ethnicity(), matchProfile.gender().toString(), matchProfile.personalityType().toString(), matchProfile.bio(),
                        user.firstName(), user.lastName(), user.age(), user.ethnicity(), user.gender().toString(), user.personalityType().toString(), user.bio());

        SystemMessage systemMessage = new SystemMessage(systemMessagePrompt);

        //CONVERT ChatMessage -> ASSISTANT / USER MESSAGE
        List<AbstractMessage> conversationMessages = conversation.messages().stream().map(chatMessage -> {
            if (chatMessage.authorId().equals(matchProfile.id())) {
                return new AssistantMessage(chatMessage.text());
            } else {
                return new UserMessage(chatMessage.text());
            }
        }).toList();

        List<Message> allMessages = new ArrayList<>();
        allMessages.add(systemMessage);
        allMessages.addAll(conversationMessages);


        Prompt prompt = new Prompt(allMessages);
        ChatResponse response = ollamaChatModel.call(prompt);

        //CONVERT AI RESPONSE TO ChatMessage
        conversation.messages().add(new ChatMessage(
                matchProfile.id(),
                response.getResult().getOutput().getContent(),
                LocalDateTime.now()
        ));

        return conversation;
    }

}
