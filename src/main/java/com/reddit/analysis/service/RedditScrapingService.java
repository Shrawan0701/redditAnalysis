package com.reddit.analysis.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reddit.analysis.model.AnalysisRequest;
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

    @Autowired
    private RedditAuthService redditAuthService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<PostData> scrapeRedditData(AnalysisRequest request) throws Exception {
        if ("thread".equals(request.getAnalysisType())) {
            return scrapeThreadData(request);
        } else {
            return scrapeSubredditData(request);
        }
    }

    private List<PostData> scrapeThreadData(AnalysisRequest request) throws Exception {
        List<PostData> posts = new ArrayList<>();
        String cleanUrl = cleanThreadUrl(request.getInput());
        String jsonUrl = cleanUrl + ".json";

        System.out.println("Fetching thread data from: " + jsonUrl);

        int maxRetries = 3;          // Number of retries
        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetries) {
            try {
                String jsonResponse = webClient.get()
                        .uri(jsonUrl)
                        .header("Authorization", "Bearer " + redditAuthService.getAccessToken())
                        .header("User-Agent", "PostAnalysisBot/1.0 by u/Shrawann_07")
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                return parseThreadJson(jsonResponse);

            } catch (Exception e) {
                lastException = e;
                attempt++;
                System.err.println("Attempt " + attempt + " failed: " + e.getMessage());
                Thread.sleep(1000 * attempt); // exponential backoff: 1s, 2s, 3s
            }
        }

        // If all retries fail, throw last exception
        throw lastException;
    }


    private String cleanThreadUrl(String url) {
        if (url.contains("?")) url = url.substring(0, url.indexOf("?"));
        if (!url.endsWith("/")) url += "/";
        return url;
    }

    private List<PostData> scrapeSubredditData(AnalysisRequest request) throws Exception {
        List<PostData> allPosts = new ArrayList<>();
        String[] sortTypes = {"hot", "top", "new"};
        int postsPerSort = 35;

        for (String sortType : sortTypes) {
            try {
                String apiUrl = buildRedditApiUrl(request, sortType, postsPerSort); // proper variable
                String jsonResponse = webClient.get()
                        .uri(apiUrl)
                        .header("Authorization", "Bearer " + redditAuthService.getAccessToken())
                        .header("User-Agent", "PostAnalysisBot/1.0 by u/Shrawann_07")
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                List<PostData> posts = parseRedditJson(jsonResponse);
                allPosts.addAll(posts);

                Thread.sleep(1000); // prevent rate-limit
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
        String subredditName = extractSubredditName(request.getInput().trim());
        return String.format("https://oauth.reddit.com/r/%s/%s?limit=%d", subredditName, sortType, limit);
    }


    private String extractSubredditName(String input) {
        if (input.contains("reddit.com/r/")) {
            String subredditName = input.substring(input.indexOf("/r/") + 3);
            if (subredditName.contains("/")) subredditName = subredditName.substring(0, subredditName.indexOf("/"));
            return subredditName;
        } else if (input.startsWith("r/")) return input.substring(2);
        else return input;
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
                        PostData mainPost = parsePost(child.get("data"));
                        JsonNode commentsListing = rootNode.get(1);
                        mainPost.setRealCommentCount(countComments(commentsListing));
                        posts.add(mainPost);
                    }
                }
            }
        }
        return posts;
    }

    private List<PostData> parseRedditJson(String jsonResponse) throws Exception {
        List<PostData> posts = new ArrayList<>();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        if (rootNode.isArray()) {
            for (JsonNode node : rootNode) {
                if (node.has("data") && node.get("data").has("children")) {
                    posts.addAll(parsePostsFromChildren(node.get("data").get("children")));
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
            if (child.has("data") && "t3".equals(child.path("kind").asText())) {
                posts.add(parsePost(child.get("data")));
            }
        }
        return posts;
    }

    private PostData parsePost(JsonNode data) {
        PostData post = new PostData();
        post.setId(data.path("id").asText(""));
        post.setTitle(data.path("title").asText(""));
        String content = data.has("selftext") && !data.get("selftext").asText().isEmpty()
                ? data.get("selftext").asText()
                : (data.has("url") && !data.get("url").asText().isEmpty()
                ? (data.get("url").asText().contains("reddit.com") ? "Discussion thread" : "Link post: " + data.get("url").asText())
                : "Title-only post");
        post.setContent(content);
        post.setAuthor(data.path("author").asText("unknown"));
        post.setUpvotes(data.path("ups").asInt(0));
        post.setDownvotes(0);
        if (data.has("created_utc")) {
            post.setCreatedTime(LocalDateTime.ofInstant(Instant.ofEpochSecond(data.get("created_utc").asLong()), ZoneId.systemDefault()));
        }
        post.setRealCommentCount(data.path("num_comments").asInt(0));
        post.setComments(new ArrayList<>());
        return post;
    }

    private int countComments(JsonNode commentsListing) {
        int count = 0;
        if (commentsListing.has("data") && commentsListing.get("data").has("children")) {
            count = countCommentsRecursive(commentsListing.get("data").get("children"));
        }
        return count;
    }

    private int countCommentsRecursive(JsonNode children) {
        int count = 0;
        for (JsonNode child : children) {
            if ("t1".equals(child.path("kind").asText())) {
                count++;
                JsonNode replies = child.path("data").path("replies");
                if (!replies.isMissingNode() && replies.has("data") && replies.get("data").has("children")) {
                    count += countCommentsRecursive(replies.get("data").get("children"));
                }
            }
        }
        return count;
    }
}
