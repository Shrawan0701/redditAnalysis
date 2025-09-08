package com.reddit.analysis.model;

import java.time.LocalDateTime;
import java.util.List;

public class PostData {
    private String id;
    private String title;
    private String content;
    private String author;
    private int upvotes;
    private int downvotes;
    private LocalDateTime createdTime;
    private List<CommentData> comments;
    private String sentiment;


    private int realCommentCount;

    public PostData() {}

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public int getDownvotes() {
        return downvotes;
    }

    public void setDownvotes(int downvotes) {
        this.downvotes = downvotes;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public List<CommentData> getComments() {
        return comments;
    }

    public void setComments(List<CommentData> comments) {
        this.comments = comments;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public int getRealCommentCount() {
        return realCommentCount;
    }

    public void setRealCommentCount(int realCommentCount) {
        this.realCommentCount = realCommentCount;
    }
}
