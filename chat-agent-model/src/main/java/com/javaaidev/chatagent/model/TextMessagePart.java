package com.javaaidev.chatagent.model;

/**
 * Text content
 *
 * @param text text
 */
public record TextMessagePart(String text) implements ThreadUserMessagePart,
    ThreadAssistantMessagePart {

  @Override
  public String getType() {
    return "text";
  }
}
