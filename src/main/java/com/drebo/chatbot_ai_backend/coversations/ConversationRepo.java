package com.drebo.chatbot_ai_backend.coversations;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConversationRepo extends MongoRepository<Conversation, String> {
}
