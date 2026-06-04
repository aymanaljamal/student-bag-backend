package com.studentbag.backend.chatbot.service.impl;

import com.studentbag.backend.chatbot.config.OpenAiProperties;
import com.studentbag.backend.chatbot.dto.openai.OpenAiChatRequest;
import com.studentbag.backend.chatbot.dto.openai.OpenAiChatResponse;
import com.studentbag.backend.chatbot.dto.openai.OpenAiMessageDto;
import com.studentbag.backend.chatbot.exception.AiProviderException;
import com.studentbag.backend.chatbot.service.OpenAiClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiClientServiceImpl implements OpenAiClientService {

    private final OpenAiProperties properties;

    private final WebClient webClient = WebClient.builder().build();

    @Override
    public OpenAiChatResponse sendChatRequest(OpenAiChatRequest request) {
        try {
            validateOpenAiConfig();

            Map<String, Object> body = Map.of(
                    "model", request.getModel() != null ? request.getModel() : properties.getModel(),
                    "messages", toMessages(request.getMessages()),
                    "temperature", request.getTemperature() != null ? request.getTemperature() : 0.3,
                    "max_tokens", request.getMaxTokens() != null ? request.getMaxTokens() : 900
            );

            log.info(
                    "Calling OpenAI model={} baseUrl={} keyExists={}",
                    body.get("model"),
                    properties.getBaseUrl(),
                    properties.getApiKey() != null && !properties.getApiKey().isBlank()
            );

            Map<?, ?> response = webClient.post()
                    .uri(properties.getBaseUrl() + "/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(errorBody -> {
                                        log.error(
                                                "OpenAI API error. status={} body={}",
                                                clientResponse.statusCode(),
                                                errorBody
                                        );
                                        return new AiProviderException(
                                                "OpenAI API error: " + clientResponse.statusCode() + " - " + errorBody
                                        );
                                    })
                    )
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                throw new AiProviderException("OpenAI returned an empty response.");
            }

            return OpenAiChatResponse.builder()
                    .answer(extractAnswer(response))
                    .model(extractModel(response))
                    .inputTokens(extractUsage(response, "prompt_tokens"))
                    .outputTokens(extractUsage(response, "completion_tokens"))
                    .totalTokens(extractUsage(response, "total_tokens"))
                    .build();

        } catch (AiProviderException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to call OpenAI API. realError={}", ex.getMessage(), ex);
            throw new AiProviderException("Failed to call OpenAI API: " + ex.getMessage(), ex);
        }
    }

    private void validateOpenAiConfig() {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new AiProviderException("OpenAI API key is missing.");
        }

        if (properties.getBaseUrl() == null || properties.getBaseUrl().isBlank()) {
            throw new AiProviderException("OpenAI base URL is missing.");
        }

        if (properties.getModel() == null || properties.getModel().isBlank()) {
            throw new AiProviderException("OpenAI model is missing.");
        }
    }

    private List<Map<String, String>> toMessages(List<OpenAiMessageDto> messages) {
        List<Map<String, String>> result = new ArrayList<>();

        if (messages == null || messages.isEmpty()) {
            result.add(Map.of(
                    "role", "user",
                    "content", ""
            ));
            return result;
        }

        for (OpenAiMessageDto message : messages) {
            if (message == null) {
                continue;
            }

            String role = message.getRole() == null || message.getRole().isBlank()
                    ? "user"
                    : message.getRole();

            String content = message.getContent() == null
                    ? ""
                    : message.getContent();

            result.add(Map.of(
                    "role", role,
                    "content", content
            ));
        }

        return result;
    }

    private String extractAnswer(Map<?, ?> response) {
        Object choices = response.get("choices");

        if (!(choices instanceof List<?> choicesList) || choicesList.isEmpty()) {
            return "I could not generate an answer.";
        }

        Object firstChoice = choicesList.get(0);

        if (!(firstChoice instanceof Map<?, ?> choiceMap)) {
            return "I could not generate an answer.";
        }

        Object message = choiceMap.get("message");

        if (!(message instanceof Map<?, ?> messageMap)) {
            return "I could not generate an answer.";
        }

        Object content = messageMap.get("content");

        if (content == null || String.valueOf(content).isBlank()) {
            return "I could not generate an answer.";
        }

        return String.valueOf(content).trim();
    }

    private String extractModel(Map<?, ?> response) {
        Object model = response.get("model");

        if (model == null) {
            return properties.getModel();
        }

        return String.valueOf(model);
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