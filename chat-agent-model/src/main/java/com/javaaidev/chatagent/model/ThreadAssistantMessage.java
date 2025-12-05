package com.javaaidev.chatagent.model;

import java.util.List;

/**
 * Assistant message in a thread
 * @param id Message id
 * @param content Message content
 * @see ThreadAssistantMessagePart
 */
public record ThreadAssistantMessage(String id, List<ThreadAssistantMessagePart> content) implements ThreadMessage {

  @Override
  public MessageRole getRole() {
    return MessageRole.assistant;
  }
}
