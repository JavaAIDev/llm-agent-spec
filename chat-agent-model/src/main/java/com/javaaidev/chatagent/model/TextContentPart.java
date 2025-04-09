package com.javaaidev.chatagent.model;

/**
 * Text content
 * @param text text
 */
public record TextContentPart(String text) implements ThreadUserContentPart, ThreadAssistantContentPart {

  @Override
  public String getType() {
    return "text";
  }
}
