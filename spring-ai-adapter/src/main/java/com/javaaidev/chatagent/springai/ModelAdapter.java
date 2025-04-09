package com.javaaidev.chatagent.springai;

import com.javaaidev.chatagent.model.ChatAgentRequest;
import com.javaaidev.chatagent.model.ChatAgentResponse;
import com.javaaidev.chatagent.model.TextContentPart;
import com.javaaidev.chatagent.model.ThreadAssistantContentPart;
import com.javaaidev.chatagent.model.ThreadAssistantMessage;
import com.javaaidev.chatagent.model.ThreadUserMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * Convert models between chat agent and Spring AI
 */
public class ModelAdapter {

  private ModelAdapter() {
  }

  /**
   * Convert a {@linkplain ChatAgentRequest} to a list of Spring AI {@linkplain Message}
   *
   * @param request {@linkplain ChatAgentRequest} of chat agent
   * @return A list of Spring AI {@linkplain Message}
   */
  public static List<Message> fromRequest(ChatAgentRequest request) {
    return request.messages().stream().flatMap(message -> {
      if (message instanceof ThreadUserMessage userMessage) {
        return userMessage.content().stream().map(part -> {
          if (part instanceof TextContentPart textContentPart) {
            return new UserMessage(textContentPart.text());
          }
          return null;
        }).filter(Objects::nonNull);
      } else if (message instanceof ThreadAssistantMessage assistantMessage) {
        return assistantMessage.content().stream().map(part -> {
          if (part instanceof TextContentPart textContentPart) {
            return new AssistantMessage(textContentPart.text());
          }
          return null;
        }).filter(Objects::nonNull);
      }
      return Stream.<Message>of();
    }).toList();
  }

  /**
   * Convert a Spring AI {@linkplain ChatResponse} to {@linkplain ChatAgentResponse}
   *
   * @param chatResponse Spring AI {@linkplain ChatResponse}
   * @return {@linkplain ChatAgentResponse} of chat agent
   */
  public static ChatAgentResponse toResponse(ChatResponse chatResponse) {
    var content = new ArrayList<ThreadAssistantContentPart>();
    for (Generation generation : chatResponse.getResults()) {
      content.add(new TextContentPart(generation.getOutput().getText()));
    }
    return new ChatAgentResponse(content);
  }

  /**
   * Convert a stream of Spring AI {@linkplain ChatResponse} to a stream of
   * {@linkplain ChatAgentResponse} using Server-sent Events
   *
   * @param chatResponse Stream of {@linkplain ChatResponse}
   * @return Stream of {@linkplain ChatAgentResponse}
   */
  public static Flux<ServerSentEvent<ChatAgentResponse>> toStreamingResponse(
      Flux<ChatResponse> chatResponse) {
    return chatResponse.concatMap(
            response -> Flux.fromIterable(response.getResults()))
        .filter(generation -> Objects.nonNull(generation.getOutput().getText()))
        .map(generation -> generation.getOutput().getText())
        .map(text -> ServerSentEvent.<ChatAgentResponse>builder()
            .data(new ChatAgentResponse(
                List.of(new TextContentPart(text))))
            .build());
  }
}
