package site.zqiusu.domain.agent.service.armory.matter.patch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.adk.models.LlmRequest;
import com.google.adk.models.springai.MessageConverter;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.util.MimeType;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class MyMessageConverter extends MessageConverter {
    public MyMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public Prompt toLlmPrompt(LlmRequest llmRequest) {
        List<Media> mediaList = new ArrayList<>();
        for (Content content : llmRequest.contents()){
            for (Part part : content.parts().orElse(List.of())) {
                if (part.inlineData().isPresent()) {
                    // Handle inline media data (images, audio, video, etc.)
                    com.google.genai.types.Blob blob = part.inlineData().get();
                    if (blob.mimeType().isPresent() && blob.data().isPresent()) {
                        try {
                            MimeType mimeType = MimeType.valueOf(blob.mimeType().get());
                            // Create Media object from inline data using ByteArrayResource
                            org.springframework.core.io.ByteArrayResource resource =
                                    new org.springframework.core.io.ByteArrayResource(blob.data().get());
                            mediaList.add(new Media(mimeType, resource));
                        } catch (Exception e) {
                            // Log warning but continue processing other parts
                            // In production, consider proper logging framework
                            System.err.println(
                                    "Warning: Failed to parse media mime type: " + blob.mimeType().get());
                        }
                    }
                }
            }
        }

        Prompt llmPrompt = super.toLlmPrompt(llmRequest);
        llmPrompt.getUserMessage().getMedia().addAll(mediaList);
        return llmPrompt;
    }
}
