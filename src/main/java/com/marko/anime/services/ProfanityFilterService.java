package com.marko.anime.services;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ProfanityFilterService {

    @Value("${API_KEY}")
    private String API_KEY;
    private static final String PERSPECTIVE_API_URL = "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=";
    private final RestTemplate restTemplate;

    private static final double PROFANITY_LIMIT = 0.5;
    private static final double TOXICITY_LIMIT = 0.5;

    public ProfanityFilterService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean hasProfanity(String comment) {
        String apiUrl = PERSPECTIVE_API_URL + API_KEY;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("comment", Map.of("text", comment));
        requestBody.put("languages", List.of("en"));
        requestBody.put("requestedAttributes", Map.of("PROFANITY", Map.of(), "TOXICITY", Map.of()));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("attributeScores")) {
                Map<String, Object> attributeScores = (Map<String, Object>) responseBody.get("attributeScores");

                Map<String, Object> profanity = (Map<String, Object>) attributeScores.get("PROFANITY");
                Map<String, Object> toxicity = (Map<String, Object>) attributeScores.get("TOXICITY");

                Map<String, Object> profanityScore = (Map<String, Object>) profanity.get("summaryScore");
                Map<String, Object> toxicityScore = (Map<String, Object>) toxicity.get("summaryScore");

                double profanitySummaryScore = (double) profanityScore.get("value");
                double toxicitySummaryScore = (double) toxicityScore.get("value");

                return profanitySummaryScore > PROFANITY_LIMIT || toxicitySummaryScore > TOXICITY_LIMIT;
            }
        } catch (RestClientException e) {
            throw new RestClientException("Error calling Perspective API: " + e.getMessage());
        }
        return false;
    }
}