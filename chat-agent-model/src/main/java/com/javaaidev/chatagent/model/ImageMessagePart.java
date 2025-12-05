package com.javaaidev.chatagent.model;

public record ImageMessagePart(String image, String filename) implements ThreadUserMessagePart,
    ThreadAssistantMessagePart {

  public ImageMessagePart(String image) {
    this(image, null);
  }

  @Override
  public String getType() {
    return "image";
  }
}
