package com.javaaidev.chatagent.model;

public record FileMessagePart(String data, String mimeType, String filename) implements
    ThreadUserMessagePart,
    ThreadAssistantMessagePart {

  public FileMessagePart(String data, String mimeType) {
    this(data, mimeType, null);
  }

  @Override
  public String getType() {
    return "file";
  }
}
