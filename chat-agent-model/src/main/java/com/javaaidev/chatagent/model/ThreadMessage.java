package com.javaaidev.chatagent.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "role")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ThreadUserMessage.class, name = "user"),
    @JsonSubTypes.Type(value = ThreadAssistantMessage.class, name = "assistant")
})
public interface ThreadMessage {
  MessageRole getRole();
}
