package com.javaaidev.chatagent.springai;

import com.javaaidev.chatagent.model.Attachment;
import com.javaaidev.chatagent.model.ChatAgentRequest;
import com.javaaidev.chatagent.model.ChatAgentResponse;
import com.javaaidev.chatagent.model.FileMessagePart;
import com.javaaidev.chatagent.model.ImageMessagePart;
import com.javaaidev.chatagent.model.ReasoningMessagePart;
import com.javaaidev.chatagent.model.TextMessagePart;
import com.javaaidev.chatagent.model.ThreadAssistantMessage;
import com.javaaidev.chatagent.model.ThreadAssistantMessagePart;
import com.javaaidev.chatagent.model.ThreadUserMessage;
import com.javaaidev.chatagent.model.ThreadUserMessagePart;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.content.Media;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeType;
import org.springframework.util.StringUtils;
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
    return request.messages().stream().map(message -> {
      if (message instanceof ThreadUserMessage userMessage) {
        var textBuilder = new StringBuilder();
        var mediaList = new ArrayList<Media>();
        if (!CollectionUtils.isEmpty(userMessage.content())) {
          for (ThreadUserMessagePart part : userMessage.content()) {
            if (part instanceof TextMessagePart textContentPart) {
              textBuilder.append(textContentPart.text());
            } else if (part instanceof FileMessagePart fileMessagePart) {
              mediaList.add(Media.builder()
                  .mimeType(MimeType.valueOf(fileMessagePart.mimeType()))
                  .data(fileMessagePart.data()).build());
            }
          }
        }
        if (!CollectionUtils.isEmpty(userMessage.attachments())) {
          for (Attachment attachment : userMessage.attachments()) {
            var mimeType = MimeType.valueOf(attachment.contentType());
            if (!CollectionUtils.isEmpty(attachment.content())) {
              for (ThreadUserMessagePart part : attachment.content()) {
                if (part instanceof ImageMessagePart imageMessagePart) {
                  mediaList.add(
                      Media.builder().mimeType(mimeType).data(imageMessagePart.image()).build());
                } else if (part instanceof FileMessagePart fileMessagePart) {
                  mediaList.add(Media.builder()
                      .mimeType(mimeType)
                      .data(fileMessagePart.data()).build());
                }
              }
            }
          }
        }
        return (Message) UserMessage.builder()
            .text(textBuilder.toString())
            .media(mediaList)
            .build();
      } else if (message instanceof ThreadAssistantMessage assistantMessage) {
        var textBuilder = new StringBuilder();
        for (ThreadAssistantMessagePart part : assistantMessage.content()) {
          if (part instanceof TextMessagePart textContentPart) {
            textBuilder.append(textContentPart.text());
          }
        }
        return AssistantMessage.builder().content(textBuilder.toString()).build();
      }
      return null;
    }).filter(Objects::nonNull).toList();
  }

  /**
   * Convert a Spring AI {@linkplain ChatResponse} to {@linkplain ChatAgentResponse}
   *
   * @param chatResponse Spring AI {@linkplain ChatResponse}
   * @return {@linkplain ChatAgentResponse} of chat agent
   */
  public static ChatAgentResponse toResponse(ChatResponse chatResponse) {
    var content = new ArrayList<ThreadAssistantMessagePart>();
    for (Generation generation : chatResponse.getResults()) {
      var message = generation.getOutput();
      if (StringUtils.hasText(message.getText())) {
        content.add(new TextMessagePart(message.getText()));
      }
      extractReasoning(message).ifPresent(
          reasoning -> content.add(new ReasoningMessagePart(reasoning))
      );
      if (!CollectionUtils.isEmpty(message.getMedia())) {
        message.getMedia().forEach(media -> {
          var mimeType = media.getMimeType();
          if (isImage(mimeType)) {
            content.add(new ImageMessagePart(fromMediaData(mimeType, media.getData())));
          } else {
            content.add(new FileMessagePart(fromMediaData(mimeType, media.getData()),
                mimeType.toString()));
          }
        });
      }
    }
    return new ChatAgentResponse(content);
  }

  private static Optional<String> extractReasoning(AssistantMessage assistantMessage) {
    var metadata = assistantMessage.getMetadata();
    return Stream.of("reasoningContent", "thinking")
        .map(metadata::get)
        .filter(Objects::nonNull)
        .map(Object::toString)
        .filter(StringUtils::hasText)
        .findFirst();
  }

  private static boolean isImage(MimeType mimeType) {
    return "image".equalsIgnoreCase(mimeType.getType());
  }

  private static String fromMediaData(MimeType mimeType, Object data) {
    if (data instanceof byte[] bytes) {
      if (isImage(mimeType)) {
        return String.format("data:%s;base64,%s", mimeType,
            Base64.getEncoder().encodeToString(bytes));
      } else {
        return Base64.getEncoder().encodeToString(bytes);
      }
    } else if (data instanceof String text) {
      return text;
    } else {
      throw new IllegalArgumentException(
          "Unsupported media data type: " + data.getClass().getSimpleName());
    }
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
    return chatResponse.map(ModelAdapter::toResponse)
        .filter(ChatAgentResponse::hasContent)
        .map(response -> ServerSentEvent.<ChatAgentResponse>builder()
            .data(response)
            .build());
  }
}
