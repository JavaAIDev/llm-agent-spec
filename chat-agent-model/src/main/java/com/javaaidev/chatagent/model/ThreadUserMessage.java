package com.javaaidev.chatagent.model;

import java.util.List;

public record ThreadUserMessage(String id, List<ThreadUserContentPart> content) implements ThreadMessage {

  @Override
  public MessageRole getRole() {
    return MessageRole.user;
  }
}
