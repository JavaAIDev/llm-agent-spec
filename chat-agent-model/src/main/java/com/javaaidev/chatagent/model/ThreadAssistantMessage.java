package com.javaaidev.chatagent.model;

import java.util.List;

/**
 * Assistant message in a thread
 * @param id Message id
 * @param content Message content
 * @see ThreadAssistantContentPart
 */
public record ThreadAssistantMessage(String id, List<ThreadAssistantContentPart> content) implements ThreadMessage {

  @Override
  public MessageRole getRole() {
    return MessageRole.assistant;
  }
}
