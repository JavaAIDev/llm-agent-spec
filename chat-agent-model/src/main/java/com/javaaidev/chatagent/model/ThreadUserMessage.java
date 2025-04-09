package com.javaaidev.chatagent.model;

import java.util.List;

/**
 * User message in a thread
 * @param id Message id
 * @param content Message content
 * @see ThreadUserContentPart
 */
public record ThreadUserMessage(String id, List<ThreadUserContentPart> content) implements ThreadMessage {

  @Override
  public MessageRole getRole() {
    return MessageRole.user;
  }
}
