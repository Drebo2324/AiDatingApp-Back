package com.drebo.chatbot_ai_backend.matches;

import com.drebo.chatbot_ai_backend.profiles.Profile;

public record Match(String id, Profile profile, String conversationId
) {

}
