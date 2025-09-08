package com.reddit.analysis.model;

public class SentimentAnalysis {
    private double positivePercentage;
    private double neutralPercentage;
    private double negativePercentage;
    private String overallSentiment;

    public SentimentAnalysis() {}

    public SentimentAnalysis(double positivePercentage, double neutralPercentage, 
                           double negativePercentage, String overallSentiment) {
        this.positivePercentage = positivePercentage;
        this.neutralPercentage = neutralPercentage;
        this.negativePercentage = negativePercentage;
        this.overallSentiment = overallSentiment;
    }

    public double getPositivePercentage() {
        return positivePercentage;
    }

    public void setPositivePercentage(double positivePercentage) {
        this.positivePercentage = positivePercentage;
    }

    public double getNeutralPercentage() {
        return neutralPercentage;
    }

    public void setNeutralPercentage(double neutralPercentage) {
        this.neutralPercentage = neutralPercentage;
    }

    public double getNegativePercentage() {
        return negativePercentage;
    }

    public void setNegativePercentage(double negativePercentage) {
        this.negativePercentage = negativePercentage;
    }

    public String getOverallSentiment() {
        return overallSentiment;
    }

    public void setOverallSentiment(String overallSentiment) {
        this.overallSentiment = overallSentiment;
    }
}