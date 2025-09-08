package com.reddit.analysis.service;

import com.reddit.analysis.model.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataProcessingService {

    public AnalysisStats calculateStats(List<PostData> posts) {
        AnalysisStats stats = new AnalysisStats();

        stats.setTotalPosts(posts.size());

        // Use the dedicated realCommentCount field
        int totalComments = posts.stream()
                .mapToInt(PostData::getRealCommentCount)
                .sum();

        stats.setTotalComments(totalComments);

        Set<String> uniqueUsers = posts.stream()
                .map(PostData::getAuthor)
                .filter(Objects::nonNull)
                .filter(author -> !"unknown".equals(author))
                .collect(Collectors.toSet());

        stats.setTotalUsers(uniqueUsers.size());

        PostData mostUpvoted = posts.stream()
                .max(Comparator.comparingInt(PostData::getUpvotes))
                .orElse(null);
        if (mostUpvoted != null) {
            stats.setMostUpvotedPost(mostUpvoted.getTitle());
        }

        double averageUpvotes = posts.stream()
                .mapToInt(PostData::getUpvotes)
                .average()
                .orElse(0.0);
        stats.setAverageScore(Math.round(averageUpvotes * 10.0) / 10.0);

        return stats;
    }



    public SentimentAnalysis analyzeSentiment(List<PostData> posts) {
        int positive = 0, neutral = 0, negative = 0;

        // Enhanced keyword lists for better sentiment detection
        List<String> positiveKeywords = Arrays.asList(
                "good", "great", "excellent", "amazing", "love", "awesome", "fantastic",
                "wonderful", "perfect", "best", "happy", "excited", "success", "achievement",
                "growth", "opportunity", "helpful", "solved", "working", "easy", "smooth",
                "recommend", "impressed", "satisfied", "brilliant", "outstanding"
        );

        List<String> negativeKeywords = Arrays.asList(
                "bad", "terrible", "awful", "hate", "worst", "horrible", "annoying",
                "frustrated", "angry", "disappointed", "useless", "broken", "failed",
                "problem", "issue", "bug", "error", "difficult", "hard", "struggle",
                "reject", "fired", "unemployment", "stress", "worry", "concern"
        );

        for (PostData post : posts) {
            String content = (post.getTitle() + " " + post.getContent()).toLowerCase();

            long positiveCount = positiveKeywords.stream()
                    .mapToLong(keyword -> countOccurrences(content, keyword))
                    .sum();

            long negativeCount = negativeKeywords.stream()
                    .mapToLong(keyword -> countOccurrences(content, keyword))
                    .sum();

            if (positiveCount > negativeCount + 1) {
                positive++;
                post.setSentiment("positive");
            } else if (negativeCount > positiveCount + 1) {
                negative++;
                post.setSentiment("negative");
            } else {
                neutral++;
                post.setSentiment("neutral");
            }
        }

        int total = posts.size();
        if (total == 0) {
            return new SentimentAnalysis(0, 100, 0, "neutral");
        }

        double positivePerc = Math.round((positive * 100.0) / total * 10.0) / 10.0;
        double neutralPerc = Math.round((neutral * 100.0) / total * 10.0) / 10.0;
        double negativePerc = Math.round((negative * 100.0) / total * 10.0) / 10.0;

        String overall;
        if (positivePerc > negativePerc && positivePerc > neutralPerc) {
            overall = "positive";
        } else if (negativePerc > positivePerc && negativePerc > neutralPerc) {
            overall = "negative";
        } else {
            overall = "neutral";
        }

        return new SentimentAnalysis(positivePerc, neutralPerc, negativePerc, overall);
    }

    private long countOccurrences(String text, String keyword) {
        return Arrays.stream(text.split("\\W+"))
                .mapToLong(word -> word.equals(keyword) ? 1 : 0)
                .sum();
    }

    public List<String> extractTopics(List<PostData> posts) {
        Map<String, Integer> topicFrequency = new HashMap<>();


        Set<String> relevantTopics = new HashSet<>(Arrays.asList(
                "javascript", "python", "java", "react", "nodejs", "angular", "vue",
                "aws", "azure", "docker", "kubernetes", "microservices", "api", "database",
                "frontend", "backend", "fullstack", "devops", "mobile", "android", "ios",
                "machine", "learning", "data", "science", "artificial", "intelligence",
                "startup", "company", "job", "interview", "salary", "career", "switch",
                "remote", "work", "team", "project", "experience", "skills", "coding",
                "programming", "development", "software", "engineering", "technical"
        ));

        for (PostData post : posts) {
            String text = (post.getTitle() + " " + post.getContent()).toLowerCase();
            String[] words = text.split("\\W+");

            for (String word : words) {
                if (word.length() > 3 && (relevantTopics.contains(word) || !isCommonWord(word))) {
                    topicFrequency.put(word, topicFrequency.getOrDefault(word, 0) + 1);
                }
            }
        }

        return topicFrequency.entrySet().stream()
                .filter(entry -> entry.getValue() > 1) // Only topics mentioned more than once
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(15)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public Map<String, Integer> getKeywordFrequency(List<PostData> posts) {
        Map<String, Integer> frequency = new HashMap<>();

        for (PostData post : posts) {
            String text = (post.getTitle() + " " + post.getContent()).toLowerCase();
            String[] words = text.split("\\W+");

            for (String word : words) {
                if (word.length() > 3 && !isCommonWord(word)) {
                    frequency.put(word, frequency.getOrDefault(word, 0) + 1);
                }
            }
        }

        return frequency.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(20)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public String combinePostsText(List<PostData> posts) {
        StringBuilder combined = new StringBuilder();

        for (PostData post : posts) {
            if (post.getTitle() != null && !post.getTitle().isEmpty()) {
                combined.append("Title: ").append(post.getTitle()).append("\n");
            }
            if (post.getContent() != null && !post.getContent().isEmpty()) {
                combined.append("Content: ").append(post.getContent()).append("\n");
            }
            combined.append("Author: ").append(post.getAuthor())
                    .append(", Upvotes: ").append(post.getUpvotes()).append("\n");
            combined.append("---\n");
        }

        // Limit text for LLM but include more content
        String result = combined.toString();
        if (result.length() > 6000) {
            result = result.substring(0, 6000) + "\n... [Additional posts analyzed but truncated for processing]";
        }

        return result;
    }

    private boolean isCommonWord(String word) {
        Set<String> commonWords = new HashSet<>(Arrays.asList(
                "the", "and", "for", "are", "but", "not", "you", "all", "can", "had",
                "her", "was", "one", "our", "out", "day", "get", "has", "him", "his",
                "how", "man", "new", "now", "old", "see", "two", "way", "who", "boy",
                "did", "its", "let", "put", "say", "she", "too", "use", "have", "this",
                "that", "with", "they", "will", "your", "from", "what", "were", "been",
                "their", "said", "each", "which", "there", "would", "make", "like",
                "into", "time", "very", "when", "come", "may", "take", "them", "year"
        ));
        return commonWords.contains(word.toLowerCase());
    }
}
