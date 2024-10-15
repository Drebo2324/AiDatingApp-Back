package com.drebo.chatbot_ai_backend.coversations;

import java.time.LocalDateTime;

public record ChatMessage(
        String text,
        String authorId,
        LocalDateTime messageTime
) {
}
