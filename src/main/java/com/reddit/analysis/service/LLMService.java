package com.reddit.analysis.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Service
public class LLMService {

    private static final Logger logger = LoggerFactory.getLogger(LLMService.class);

    @Autowired
    private WebClient webClient;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateSummary(String text, String analysisType, String subredditName) {
        if (isApiKeyInvalid()) {
            return "Please configure your Gemini API key in application.properties to enable AI-powered insights.";
        }

        String prompt = buildSummaryPrompt(text, analysisType, subredditName);
        String response = callGeminiAPI(prompt);
        return cleanResponse(response);
    }

    public String generateBusinessInsights(String text, List<String> keyTopics, String subredditName) {
        if (isApiKeyInvalid()) {
            return "Please configure your Gemini API key in application.properties to enable AI-powered business insights.";
        }

        String prompt = buildBusinessInsightsPrompt(text, keyTopics, subredditName);
        String response = callGeminiAPI(prompt);
        return cleanResponse(response);
    }

    private boolean isApiKeyInvalid() {
        return geminiApiKey == null || geminiApiKey.trim().isEmpty()
                || "YOUR_GEMINI_API_KEY_HERE".equals(geminiApiKey);
    }

    private String buildSummaryPrompt(String text, String analysisType, String subredditName) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the following Reddit posts from r/").append(subredditName)
                .append(" and provide a comprehensive professional summary. ")
                .append("Focus on the community's trends, challenges, and opportunities. ")
                .append("Write in clean, readable paragraphs without any markdown formatting or special characters.\n\n");

        prompt.append("Posts to analyze:\n");
        prompt.append(text.length() > 4000 ? text.substring(0, 4000) + "..." : text);

        prompt.append("\n\nProvide analysis covering:\n");
        prompt.append("1. Key themes and trending topics within the subreddit\n");
        prompt.append("2. Common challenges faced by the community\n");
        prompt.append("3. Career and industry trends if applicable\n");
        prompt.append("4. Technical discussions and innovations\n");
        prompt.append("5. Community sentiment and engagement patterns\n\n");

        prompt.append("Write the response in clean, professional language suitable for business reporting. ");
        prompt.append("Use proper paragraphs and avoid any special formatting characters.");

        return prompt.toString();
    }

    private String buildBusinessInsightsPrompt(String text, List<String> keyTopics, String subredditName) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Based on discussions in r/").append(subredditName)
                .append(", provide actionable business insights for companies, recruiters, and stakeholders. ")
                .append("Write in clean, readable format without any markdown or special characters.\n\n");

        prompt.append("Key discussion topics identified: ");
        prompt.append(String.join(", ", keyTopics));
        prompt.append("\n\nContent summary:\n");
        prompt.append(text.length() > 4000 ? text.substring(0, 4000) + "..." : text);

        prompt.append("\n\nProvide specific business insights on:\n");
        prompt.append("1. Talent acquisition and retention strategies for this community\n");
        prompt.append("2. Community pain points that should be addressed\n");
        prompt.append("3. Emerging trends and technologies\n");
        prompt.append("4. Market opportunities and gaps\n");
        prompt.append("5. User behavior and engagement patterns\n");
        prompt.append("6. Skills gaps and educational opportunities\n\n");

        prompt.append("Format as clear, actionable recommendations that business leaders can implement. ");
        prompt.append("Use professional language suitable for executive briefings.");

        return prompt.toString();
    }

    private String cleanResponse(String response) {
        if (response == null) return "No response generated.";

        return response
                .replaceAll("\\*\\*([^*]+)\\*\\*", "$1")  // Remove bold
                .replaceAll("\\*([^*]+)\\*", "$1")        // Remove italic
                .replaceAll("#{1,6}\\s*", "")             // Remove headers
                .replaceAll("``````", "")                 // Remove code blocks
                .replaceAll("`([^`]+)`", "$1")            // Remove inline code
                .replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", "$1") // Remove links
                .trim();
    }

    private String callGeminiAPI(String prompt) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            ArrayNode contentsArray = objectMapper.createArrayNode();
            ObjectNode contentObject = objectMapper.createObjectNode();
            ArrayNode partsArray = objectMapper.createArrayNode();
            ObjectNode partObject = objectMapper.createObjectNode();

            partObject.put("text", prompt);
            partsArray.add(partObject);
            contentObject.set("parts", partsArray);
            contentsArray.add(contentObject);
            requestBody.set("contents", contentsArray);

            ObjectNode generationConfig = objectMapper.createObjectNode();
            generationConfig.put("temperature", 0.6);
            generationConfig.put("topK", 32);
            generationConfig.put("topP", 0.8);
            generationConfig.put("maxOutputTokens", 1500);
            requestBody.set("generationConfig", generationConfig);

            String apiEndpoint = geminiApiUrl + "?key=" + geminiApiKey;
            logger.info("Calling Gemini API: {}", maskKey(apiEndpoint));

            String response = webClient.post()
                    .uri(apiEndpoint)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode responseNode = objectMapper.readTree(response);
            if (responseNode.has("candidates") && responseNode.get("candidates").size() > 0) {
                JsonNode candidate = responseNode.get("candidates").get(0);
                if (candidate.has("content") && candidate.get("content").has("parts")) {
                    JsonNode parts = candidate.get("content").get("parts");
                    if (parts.size() > 0 && parts.get(0).has("text")) {
                        return parts.get(0).get("text").asText();
                    }
                }
            }

            return "AI analysis completed but response format was unexpected.";

        } catch (WebClientResponseException e) {
            logger.error("Gemini API error: Status {} - {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            return "Gemini API error: " + e.getRawStatusCode() + " - please try again later.";
        } catch (Exception e) {
            logger.error("Unexpected error while calling Gemini API", e);
            return "Error generating insights: " + e.getMessage();
        }
    }

    private String maskKey(String url) {
        return url.replaceAll("(key=)[^&]+", "$1****");
    }
}
