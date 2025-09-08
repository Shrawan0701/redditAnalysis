package com.reddit.analysis.service;

import com.reddit.analysis.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RedditAnalysisService {

    @Autowired
    private RedditScrapingService redditScrapingService;

    @Autowired
    private LLMService llmService;

    @Autowired
    private DataProcessingService dataProcessingService;

    public AnalysisResponse performAnalysis(AnalysisRequest request) throws Exception {
        AnalysisResponse response = new AnalysisResponse();
        response.setInputSource(request.getInput());
        response.setAnalysisType(request.getAnalysisType());

        // Scrape Reddit data
        List<PostData> posts = redditScrapingService.scrapeRedditData(request);

        // Process data and extract insights
        SentimentAnalysis sentiment = dataProcessingService.analyzeSentiment(posts);
        List<String> keyTopics = dataProcessingService.extractTopics(posts);
        Map<String, Integer> keywordFreq = dataProcessingService.getKeywordFrequency(posts);
        AnalysisStats stats = dataProcessingService.calculateStats(posts);

        // Extract subreddit name for AI analysis
        String subredditName = "unknown";
        if (request.getInput() != null) {
            String input = request.getInput().trim();
            if (input.contains("reddit.com/r/")) {
                subredditName = input.substring(input.indexOf("/r/") + 3);
                if (subredditName.contains("/")) {
                    subredditName = subredditName.substring(0, subredditName.indexOf("/"));
                }
            } else if (input.startsWith("r/")) {
                subredditName = input.substring(2);
            } else {
                subredditName = input;
            }
        }

        // Generate LLM insights with dynamic subreddit name
        String combinedText = dataProcessingService.combinePostsText(posts);
        String llmSummary = llmService.generateSummary(combinedText, request.getAnalysisType(), subredditName);
        String businessInsights = llmService.generateBusinessInsights(combinedText, keyTopics, subredditName);


        response.setSentimentAnalysis(sentiment);
        response.setKeyTopics(keyTopics);
        response.setKeywordFrequency(keywordFreq);
        response.setLlmSummary(llmSummary);
        response.setBusinessInsights(businessInsights);
        response.setAnalyzedPosts(posts);
        response.setStats(stats);

        return response;
    }

}