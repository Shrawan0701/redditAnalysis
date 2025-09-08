package com.reddit.analysis.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AnalysisResponse {
    private String inputSource;
    private String analysisType;
    private LocalDateTime timestamp;
    private SentimentAnalysis sentimentAnalysis;
    private List<String> keyTopics;
    private Map<String, Integer> keywordFrequency;
    private String llmSummary;
    private String businessInsights;
    private List<PostData> analyzedPosts;
    private AnalysisStats stats;

    // Constructors
    public AnalysisResponse() {
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getInputSource() {
        return inputSource;
    }

    public void setInputSource(String inputSource) {
        this.inputSource = inputSource;
    }

    public String getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(String analysisType) {
        this.analysisType = analysisType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public SentimentAnalysis getSentimentAnalysis() {
        return sentimentAnalysis;
    }

    public void setSentimentAnalysis(SentimentAnalysis sentimentAnalysis) {
        this.sentimentAnalysis = sentimentAnalysis;
    }

    public List<String> getKeyTopics() {
        return keyTopics;
    }

    public void setKeyTopics(List<String> keyTopics) {
        this.keyTopics = keyTopics;
    }

    public Map<String, Integer> getKeywordFrequency() {
        return keywordFrequency;
    }

    public void setKeywordFrequency(Map<String, Integer> keywordFrequency) {
        this.keywordFrequency = keywordFrequency;
    }

    public String getLlmSummary() {
        return llmSummary;
    }

    public void setLlmSummary(String llmSummary) {
        this.llmSummary = llmSummary;
    }

    public String getBusinessInsights() {
        return businessInsights;
    }

    public void setBusinessInsights(String businessInsights) {
        this.businessInsights = businessInsights;
    }

    public List<PostData> getAnalyzedPosts() {
        return analyzedPosts;
    }

    public void setAnalyzedPosts(List<PostData> analyzedPosts) {
        this.analyzedPosts = analyzedPosts;
    }

    public AnalysisStats getStats() {
        return stats;
    }

    public void setStats(AnalysisStats stats) {
        this.stats = stats;
    }
}