package com.group1.aeropace.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group1.aeropace.shared.exception.AppException;
import com.group1.aeropace.shared.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EmbeddingService {

    private static final String EMBED_URL_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:embedContent?key=%s";

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_RETRY_WAIT_MS = 10_000;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.embedding.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Float> embed(String text) {
        String url = String.format(EMBED_URL_TEMPLATE, model, apiKey);

        Map<String, Object> body = Map.of(
                "model", "models/" + model,
                "content", Map.of("parts", List.of(Map.of("text", text)))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        long waitMs = INITIAL_RETRY_WAIT_MS;
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                String raw = restTemplate.postForObject(url, request, String.class);
                JsonNode values = objectMapper.readTree(raw).at("/embedding/values");
                List<Float> result = new ArrayList<>(values.size());
                for (JsonNode val : values) {
                    result.add(val.floatValue());
                }
                return result;
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 429 && attempt < MAX_RETRIES) {
                    log.warn("[EmbeddingService] Rate limited (429), waiting {}s before retry {}/{}",
                            waitMs / 1000, attempt + 1, MAX_RETRIES);
                    sleep(waitMs);
                    waitMs *= 2;
                } else {
                    log.error("[EmbeddingService] Gemini API call failed (text length={}): {}", text.length(), e.getMessage());
                    throw new AppException(ErrorCode.EMBEDDING_API_ERROR);
                }
            } catch (Exception e) {
                log.error("[EmbeddingService] Gemini API call failed (text length={}): {}", text.length(), e.getMessage());
                throw new AppException(ErrorCode.EMBEDDING_API_ERROR);
            }
        }

        throw new AppException(ErrorCode.EMBEDDING_API_ERROR);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
