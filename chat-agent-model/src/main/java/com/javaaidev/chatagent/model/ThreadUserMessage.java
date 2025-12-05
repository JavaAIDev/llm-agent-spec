package com.javaaidev.chatagent.model;

import java.util.List;

/**
 * User message in a thread
 *
 * @param id          Message id
 * @param content     Message content
 * @param attachments Message attachments
 * @see ThreadUserMessagePart
 */
public record ThreadUserMessage(
    String id,
    List<ThreadUserMessagePart> content,
    List<Attachment> attachments) implements ThreadMessage {

  @Override
  public MessageRole getRole() {
    return MessageRole.user;
  }
}
