package com.javaaidev.chatagent.model;

public record TextContentPart(String text) implements ThreadUserContentPart, ThreadAssistantContentPart {

  @Override
  public String getType() {
    return "text";
  }
}
