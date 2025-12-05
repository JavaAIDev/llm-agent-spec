package com.javaaidev.chatagent.model;

import java.util.List;

public record Attachment(
    String id,
    String type,
    String name,
    String contentType,
    List<ThreadUserMessagePart> content) {

}
