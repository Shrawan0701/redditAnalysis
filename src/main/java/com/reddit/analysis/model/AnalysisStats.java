package com.reddit.analysis.model;

public class AnalysisStats {
    private int totalPosts;
    private int totalComments;
    private int totalUsers;
    private String mostActiveUser;
    private String mostUpvotedPost;
    private double averageScore;

    public AnalysisStats() {}

    // Getters and Setters
    public int getTotalPosts() {
        return totalPosts;
    }

    public void setTotalPosts(int totalPosts) {
        this.totalPosts = totalPosts;
    }

    public int getTotalComments() {
        return totalComments;
    }

    public void setTotalComments(int totalComments) {
        this.totalComments = totalComments;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public String getMostActiveUser() {
        return mostActiveUser;
    }

    public void setMostActiveUser(String mostActiveUser) {
        this.mostActiveUser = mostActiveUser;
    }

    public String getMostUpvotedPost() {
        return mostUpvotedPost;
    }

    public void setMostUpvotedPost(String mostUpvotedPost) {
        this.mostUpvotedPost = mostUpvotedPost;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }
}