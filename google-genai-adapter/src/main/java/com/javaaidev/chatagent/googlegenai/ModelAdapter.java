package com.javaaidev.chatagent.googlegenai;

import com.google.genai.types.Content;
import com.google.genai.types.Part;
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
import io.reactivex.rxjava3.core.Flowable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import org.springframework.http.codec.ServerSentEvent;

/**
 * Convert models between chat agent and Google GenAI
 */
public class ModelAdapter {

  private ModelAdapter() {
  }

  /**
   * Convert a {@linkplain ChatAgentRequest} to a {@linkplain Content}
   *
   * @param request {@linkplain ChatAgentRequest} of chat agent
   * @return {@linkplain Content}
   */
  public static Content fromRequest(ChatAgentRequest request) {
    var parts = new ArrayList<Part>();
    for (var message : request.messages()) {
      if (message instanceof ThreadUserMessage userMessage) {
        var textBuilder = new StringBuilder();
        if (userMessage.content() != null && !userMessage.content().isEmpty()) {
          for (ThreadUserMessagePart part : userMessage.content()) {
            if (part instanceof TextMessagePart textContentPart) {
              textBuilder.append(textContentPart.text());
            } else if (part instanceof FileMessagePart fileMessagePart) {
              parts.add(toPart(fileMessagePart.mimeType(), fileMessagePart.data()));
            }
          }
        }
        if (userMessage.attachments() != null && !userMessage.attachments().isEmpty()) {
          for (Attachment attachment : userMessage.attachments()) {
            var mimeType = attachment.contentType();
            if (attachment.content() != null && !attachment.content().isEmpty()) {
              for (ThreadUserMessagePart part : attachment.content()) {
                if (part instanceof ImageMessagePart imageMessagePart) {
                  parts.add(toPart(mimeType, imageMessagePart.image()));
                } else if (part instanceof FileMessagePart fileMessagePart) {
                  parts.add(toPart(mimeType, fileMessagePart.data()));
                }
              }
            }
          }
        }
        parts.add(Part.fromText(textBuilder.toString()));
      } else if (message instanceof ThreadAssistantMessage assistantMessage) {
        var textBuilder = new StringBuilder();
        for (ThreadAssistantMessagePart part : assistantMessage.content()) {
          if (part instanceof TextMessagePart textContentPart) {
            textBuilder.append(textContentPart.text());
          }
        }
        parts.add(Part.fromText(textBuilder.toString()));
      }
    }
    return Content.fromParts(parts.toArray(new Part[0]));
  }

  /**
   * Convert a {@linkplain Content} to {@linkplain ChatAgentResponse}
   *
   * @param content {@linkplain Content}
   * @return {@linkplain ChatAgentResponse} of chat agent
   */
  public static ChatAgentResponse toResponse(Content content) {
    var messageParts = new ArrayList<ThreadAssistantMessagePart>();
    if (content.parts().isPresent()) {
      for (Part part : content.parts().get()) {
        if (part.thought().isPresent() && part.thought().get()) {
          part.text().ifPresent(text -> messageParts.add(new ReasoningMessagePart(text)));
        } else {
          part.text().ifPresent(text -> messageParts.add(new TextMessagePart(text)));
        }
        if (part.inlineData().isPresent()) {
          var blob = part.inlineData().get();
          if (blob.mimeType().isPresent() && blob.data().isPresent()) {
            var mimeType = blob.mimeType().get();
            var data = blob.data().get();
            if (isImage(mimeType)) {
              messageParts.add(new ImageMessagePart(fromMediaData(mimeType, data)));
            } else {
              messageParts.add(new FileMessagePart(fromMediaData(mimeType, data), mimeType));
            }
          }
        }
      }
    }
    return new ChatAgentResponse(messageParts);
  }

  private static Part toPart(String mimeType, String data) {
    return Part.fromBytes(Base64.getEncoder().encode(data.getBytes(StandardCharsets.UTF_8)),
        mimeType);
  }

  private static boolean isImage(String mimeType) {
    return mimeType.startsWith("image/");
  }

  private static String fromMediaData(String mimeType, byte[] data) {
    if (isImage(mimeType)) {
      return String.format("data:%s;base64,%s", mimeType,
          Base64.getEncoder().encodeToString(data));
    } else {
      return Base64.getEncoder().encodeToString(data);
    }
  }

  /**
   * Convert a stream of {@linkplain Content} to a stream of {@linkplain ChatAgentResponse} using
   * Server-sent Events
   *
   * @param content Stream of {@linkplain Content}
   * @return Stream of {@linkplain ChatAgentResponse}
   */
  public static Flowable<ServerSentEvent<ChatAgentResponse>> toStreamingResponse(
      Flowable<Content> content) {
    return content.map(ModelAdapter::toResponse)
        .filter(ChatAgentResponse::hasContent)
        .map(response -> ServerSentEvent.<ChatAgentResponse>builder()
            .data(response)
            .build());
  }
}
