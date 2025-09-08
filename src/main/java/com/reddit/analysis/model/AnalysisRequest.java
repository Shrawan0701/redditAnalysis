package com.reddit.analysis.model;

import jakarta.validation.constraints.NotBlank;

public class AnalysisRequest {
    @NotBlank(message = "Input is required")
    private String input;

    private String analysisType; // "subreddit" or "thread"

    public AnalysisRequest() {}

    public AnalysisRequest(String input, String analysisType) {
        this.input = input;
        this.analysisType = analysisType;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(String analysisType) {
        this.analysisType = analysisType;
    }
}