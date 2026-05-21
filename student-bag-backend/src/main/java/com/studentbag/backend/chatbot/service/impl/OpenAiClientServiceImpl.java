package com.studentbag.backend.chatbot.service.impl;

import com.studentbag.backend.chatbot.config.OpenAiProperties;
import com.studentbag.backend.chatbot.dto.openai.OpenAiChatRequest;
import com.studentbag.backend.chatbot.dto.openai.OpenAiChatResponse;
import com.studentbag.backend.chatbot.dto.openai.OpenAiMessageDto;
import com.studentbag.backend.chatbot.exception.AiProviderException;
import com.studentbag.backend.chatbot.service.OpenAiClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAiClientServiceImpl implements OpenAiClientService {

    private final OpenAiProperties properties;

    private final WebClient webClient = WebClient.builder().build();

    @Override
    public OpenAiChatResponse sendChatRequest(OpenAiChatRequest request) {
        try {
            Map<String, Object> body = Map.of(
                    "model", request.getModel() != null ? request.getModel() : properties.getModel(),
                    "input", toInput(request.getMessages()),
                    "temperature", request.getTemperature() != null ? request.getTemperature() : 0.3,
                    "max_output_tokens", request.getMaxTokens() != null ? request.getMaxTokens() : 900
            );

            Map<?, ?> response = webClient.post()
                    .uri(properties.getBaseUrl() + "/responses")
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                throw new AiProviderException("OpenAI returned an empty response.");
            }

            return OpenAiChatResponse.builder()
                    .answer(extractAnswer(response))
                    .model(extractModel(response))
                    .inputTokens(extractUsage(response, "input_tokens"))
                    .outputTokens(extractUsage(response, "output_tokens"))
                    .totalTokens(extractUsage(response, "total_tokens"))
                    .build();

        } catch (AiProviderException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AiProviderException("Failed to call OpenAI API.", ex);
        }
    }
    private String extractModel(Map<?, ?> response) {
        Object model = response.get("model");

        if (model == null) {
            return properties.getModel();
        }

        return String.valueOf(model);
    }
    private String toInput(List<OpenAiMessageDto> messages) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (OpenAiMessageDto message : messages) {
            sb.append(message.getRole())
                    .append(": ")
                    .append(message.getContent())
                    .append("\n\n");
        }

        return sb.toString();
    }

    private String extractAnswer(Map<?, ?> response) {
        Object output = response.get("output");

        if (!(output instanceof List<?> outputList) || outputList.isEmpty()) {
            return "I could not generate an answer.";
        }

        StringBuilder answer = new StringBuilder();

        for (Object item : outputList) {
            if (!(item instanceof Map<?, ?> itemMap)) continue;

            Object content = itemMap.get("content");

            if (!(content instanceof List<?> contentList)) continue;

            for (Object contentItem : contentList) {
                if (!(contentItem instanceof Map<?, ?> contentMap)) continue;

                Object text = contentMap.get("text");

                if (text != null) {
                    answer.append(text).append("\n");
                }
            }
        }

        String finalAnswer = answer.toString().trim();

        return finalAnswer.isBlank()
                ? "I could not generate an answer."
                : finalAnswer;
    }

    private Integer extractUsage(Map<?, ?> response, String key) {
        Object usage = response.get("usage");

        if (!(usage instanceof Map<?, ?> usageMap)) {
            return 0;
        }

        Object value = usageMap.get(key);

        if (value instanceof Number number) {
            return number.intValue();
        }

        return 0;
    }
}