package com.javaaidev.chatagent.model;

import java.util.List;

/**
 * Request of a chat agent
 * @param messages A list of messages
 * @see ThreadMessage
 */
public record ChatAgentRequest(List<ThreadMessage> messages) {

}
