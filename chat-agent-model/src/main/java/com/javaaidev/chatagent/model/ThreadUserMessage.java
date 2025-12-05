package com.javaaidev.chatagent.model;

import java.util.List;

/**
 * User message in a thread
 * @param id Message id
 * @param content Message content
 * @see ThreadUserMessagePart
 */
public record ThreadUserMessage(String id, List<ThreadUserMessagePart> content) implements ThreadMessage {

  @Override
  public MessageRole getRole() {
    return MessageRole.user;
  }
}
