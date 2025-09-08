package com.reddit.analysis.controller;

import com.reddit.analysis.model.AnalysisRequest;
import com.reddit.analysis.model.AnalysisResponse;
import com.reddit.analysis.service.RedditAnalysisService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class RedditAnalysisController {

    @Autowired
    private RedditAnalysisService redditAnalysisService;

    @PostMapping("/analyze-reddit")
    public ResponseEntity<AnalysisResponse> analyzeReddit(@Valid @RequestBody AnalysisRequest request) {
        try {
            AnalysisResponse response = redditAnalysisService.performAnalysis(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {

            AnalysisResponse errorResponse = new AnalysisResponse();
            errorResponse.setInputSource(request.getInput());
            errorResponse.setAnalysisType(request.getAnalysisType());
            errorResponse.setLlmSummary("Error occurred during analysis: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Reddit Analysis Platform is running!");
    }

    @PostMapping("/analyze-subreddit")
    public ResponseEntity<AnalysisResponse> analyzeSubreddit(@RequestParam String subreddit) {
        try {
            AnalysisRequest request = new AnalysisRequest(subreddit, "subreddit");
            AnalysisResponse response = redditAnalysisService.performAnalysis(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            AnalysisResponse errorResponse = new AnalysisResponse();
            errorResponse.setInputSource(subreddit);
            errorResponse.setAnalysisType("subreddit");
            errorResponse.setLlmSummary("Error occurred during subreddit analysis: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/analyze-thread")
    public ResponseEntity<AnalysisResponse> analyzeThread(@RequestParam String threadUrl) {
        try {
            AnalysisRequest request = new AnalysisRequest(threadUrl, "thread");
            AnalysisResponse response = redditAnalysisService.performAnalysis(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            AnalysisResponse errorResponse = new AnalysisResponse();
            errorResponse.setInputSource(threadUrl);
            errorResponse.setAnalysisType("thread");
            errorResponse.setLlmSummary("Error occurred during thread analysis: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}