package com.javaaidev.chatagent.model;

public record ReasoningMessagePart(String text) implements ThreadAssistantMessagePart {

  @Override
  public String getType() {
    return "reasoning";
  }
}
