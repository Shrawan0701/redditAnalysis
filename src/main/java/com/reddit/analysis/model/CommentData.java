package com.reddit.analysis.model;

import java.time.LocalDateTime;
import java.util.List;

public class CommentData {
    private String id;
    private String content;
    private String author;
    private int upvotes;
    private LocalDateTime createdTime;
    private List<CommentData> replies;
    private String sentiment;

    public CommentData() {}

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public List<CommentData> getReplies() {
        return replies;
    }

    public void setReplies(List<CommentData> replies) {
        this.replies = replies;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }
}