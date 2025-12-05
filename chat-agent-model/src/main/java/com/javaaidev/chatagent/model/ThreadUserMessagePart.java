package com.javaaidev.chatagent.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * User content in a thread
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TextMessagePart.class, name = "text"),
    @JsonSubTypes.Type(value = ImageMessagePart.class, name = "image"),
    @JsonSubTypes.Type(value = FileMessagePart.class, name = "file"),
})
public interface ThreadUserMessagePart {

  @JsonIgnore
  String getType();
}
