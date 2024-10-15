package com.drebo.chatbot_ai_backend.coversations;

import com.drebo.chatbot_ai_backend.profiles.Profile;

import java.util.List;

public record Conversation(
        String id,
        String profileId,
        List<ChatMessage> messages
) {
}
