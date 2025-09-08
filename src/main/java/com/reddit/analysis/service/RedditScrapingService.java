package com.reddit.analysis.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reddit.analysis.model.AnalysisRequest;
import com.reddit.analysis.model.CommentData;
import com.reddit.analysis.model.PostData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class RedditScrapingService {

    @Autowired
    private WebClient webClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<PostData> scrapeRedditData(AnalysisRequest request) throws Exception {
        if ("thread".equals(request.getAnalysisType())) {
            // Handle specific thread analysis
            return scrapeThreadData(request);
        } else {
            // Handle subreddit analysis
            return scrapeSubredditData(request);
        }
    }

    private List<PostData> scrapeThreadData(AnalysisRequest request) throws Exception {
        List<PostData> posts = new ArrayList<>();

        String cleanUrl = cleanThreadUrl(request.getInput());
        String jsonUrl = cleanUrl + ".json";

        System.out.println("Fetching thread data from: " + jsonUrl);

        String jsonResponse = webClient.get()
                .uri(jsonUrl)
                .header("User-Agent", "RedditAnalysisBot/1.0 (Educational Research)")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return parseThreadJson(jsonResponse);
    }

    private String cleanThreadUrl(String url) {
        // Remove all the utm parameters and other query strings
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }

        // Ensure it ends with proper JSON fetching
        if (!url.endsWith("/")) {
            url += "/";
        }

        return url;
    }

    private List<PostData> parseThreadJson(String jsonResponse) throws Exception {
        List<PostData> posts = new ArrayList<>();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        if (rootNode.isArray() && rootNode.size() >= 2) {

            JsonNode postListing = rootNode.get(0);
            if (postListing.has("data") && postListing.get("data").has("children")) {
                JsonNode children = postListing.get("data").get("children");
                for (JsonNode child : children) {
                    if (child.has("data")) {
                        JsonNode data = child.get("data");
                        PostData mainPost = parsePost(data);

                        // Second element contains comments - count them
                        JsonNode commentsListing = rootNode.get(1);
                        int commentCount = countComments(commentsListing);
                        mainPost.setRealCommentCount(commentCount);

                        posts.add(mainPost);
                    }
                }
            }
        }

        return posts;
    }

    private int countComments(JsonNode commentsListing) {
        int count = 0;
        if (commentsListing.has("data") && commentsListing.get("data").has("children")) {
            JsonNode children = commentsListing.get("data").get("children");
            count = countCommentsRecursive(children);
        }
        return count;
    }

    private int countCommentsRecursive(JsonNode children) {
        int count = 0;
        for (JsonNode child : children) {
            if (child.has("kind") && "t1".equals(child.get("kind").asText())) {
                count++; // This is a comment

                JsonNode data = child.get("data");
                if (data.has("replies") && !data.get("replies").isNull()) {
                    JsonNode replies = data.get("replies");
                    if (replies.has("data") && replies.get("data").has("children")) {
                        count += countCommentsRecursive(replies.get("data").get("children"));
                    }
                }
            }
        }
        return count;
    }

    private List<PostData> scrapeSubredditData(AnalysisRequest request) throws Exception {
        List<PostData> allPosts = new ArrayList<>();

        String[] sortTypes = {"hot", "top", "new"};
        int postsPerSort = 35;

        for (String sortType : sortTypes) {
            try {
                String url = buildRedditApiUrl(request, sortType, postsPerSort);
                String jsonResponse = webClient.get()
                        .uri(url)
                        .header("User-Agent", "RedditAnalysisBot/1.0 (Educational Research)")
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                List<PostData> posts = parseRedditJson(jsonResponse);
                allPosts.addAll(posts);

                Thread.sleep(500);
            } catch (Exception e) {
                System.err.println("Error fetching " + sortType + " posts: " + e.getMessage());
            }
        }

        // Remove duplicates
        List<PostData> uniquePosts = new ArrayList<>();
        List<String> seenIds = new ArrayList<>();

        for (PostData post : allPosts) {
            if (!seenIds.contains(post.getId())) {
                seenIds.add(post.getId());
                uniquePosts.add(post);
            }
        }

        return uniquePosts.subList(0, Math.min(uniquePosts.size(), 100));
    }

    private String buildRedditApiUrl(AnalysisRequest request, String sortType, int limit) {
        String input = request.getInput().trim();
        String subredditName = extractSubredditName(input);
        return String.format("https://www.reddit.com/r/%s/%s.json?limit=%d&raw_json=1",
                subredditName, sortType, limit);
    }

    private String extractSubredditName(String input) {
        if (input.contains("reddit.com/r/")) {
            String subredditName = input.substring(input.indexOf("/r/") + 3);
            if (subredditName.contains("/")) {
                subredditName = subredditName.substring(0, subredditName.indexOf("/"));
            }
            return subredditName;
        } else if (input.startsWith("r/")) {
            return input.substring(2);
        } else {
            return input;
        }
    }

    private List<PostData> parseRedditJson(String jsonResponse) throws Exception {
        List<PostData> posts = new ArrayList<>();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        if (rootNode.isArray()) {
            for (JsonNode listingNode : rootNode) {
                if (listingNode.has("data") && listingNode.get("data").has("children")) {
                    posts.addAll(parsePostsFromChildren(listingNode.get("data").get("children")));
                }
            }
        } else if (rootNode.has("data") && rootNode.get("data").has("children")) {
            posts.addAll(parsePostsFromChildren(rootNode.get("data").get("children")));
        }

        return posts;
    }

    private List<PostData> parsePostsFromChildren(JsonNode children) {
        List<PostData> posts = new ArrayList<>();

        for (JsonNode child : children) {
            if (child.has("data")) {
                JsonNode data = child.get("data");
                String kind = child.has("kind") ? child.get("kind").asText() : "";

                if ("t3".equals(kind)) {
                    posts.add(parsePost(data));
                }
            }
        }

        return posts;
    }

    private PostData parsePost(JsonNode data) {
        PostData post = new PostData();

        post.setId(data.has("id") ? data.get("id").asText() : "");
        post.setTitle(data.has("title") ? data.get("title").asText() : "");

        // Better content extraction
        String content = "";
        if (data.has("selftext") && !data.get("selftext").asText().isEmpty()) {
            content = data.get("selftext").asText();
        } else if (data.has("url") && !data.get("url").asText().isEmpty()) {
            String url = data.get("url").asText();
            if (!url.contains("reddit.com")) {
                content = "Link post: " + url;
            } else {
                content = "Discussion thread";
            }
        }
        if (content.isEmpty()) {
            content = "Title-only post";
        }
        post.setContent(content);

        post.setAuthor(data.has("author") ? data.get("author").asText() : "unknown");
        post.setUpvotes(data.has("ups") ? data.get("ups").asInt() : 0);
        post.setDownvotes(0);

        if (data.has("created_utc")) {
            long timestamp = data.get("created_utc").asLong();
            post.setCreatedTime(LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(timestamp), ZoneId.systemDefault()));
        }

        int realCommentCount = data.has("num_comments") ? data.get("num_comments").asInt() : 0;
        post.setRealCommentCount(realCommentCount);

        post.setComments(new ArrayList<>());

        return post;
    }
}
