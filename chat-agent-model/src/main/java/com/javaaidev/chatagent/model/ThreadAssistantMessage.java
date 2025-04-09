package com.javaaidev.chatagent.model;

import java.util.List;

public record ThreadAssistantMessage(String id, List<ThreadAssistantContentPart> content) implements ThreadMessage {

  @Override
  public MessageRole getRole() {
    return MessageRole.assistant;
  }
}
