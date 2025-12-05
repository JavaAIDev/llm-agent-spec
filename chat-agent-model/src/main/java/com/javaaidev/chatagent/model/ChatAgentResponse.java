package com.javaaidev.chatagent.model;

import java.util.List;

/**
 * Response of a chat agent
 *
 * @param content content in the response
 * @see ThreadAssistantMessagePart
 */
public record ChatAgentResponse(List<ThreadAssistantMessagePart> content) {

  public boolean hasContent() {
    return content != null && !content.isEmpty();
  }
}
