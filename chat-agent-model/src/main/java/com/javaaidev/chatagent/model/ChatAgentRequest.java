package com.javaaidev.chatagent.model;

import java.util.List;

public record ChatAgentRequest(List<ThreadMessage> messages) {

}
