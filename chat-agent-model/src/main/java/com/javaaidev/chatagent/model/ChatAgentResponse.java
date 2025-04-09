package com.javaaidev.chatagent.model;

import java.util.List;

/**
 * Response of a chat agent
 * @param content content in the response
 * @see ThreadAssistantContentPart
 */
public record ChatAgentResponse(List<ThreadAssistantContentPart> content) {

}
