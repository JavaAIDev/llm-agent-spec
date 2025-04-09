package com.javaaidev.chatagent.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * User content in a thread
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TextContentPart.class, name = "text")
})
public interface ThreadUserContentPart {
  String getType();
}
