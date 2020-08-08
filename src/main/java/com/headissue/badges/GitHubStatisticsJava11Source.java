package com.headissue.badges;

import org.cache2k.integration.AsyncCacheLoader;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Request counters from GitHub. Async version which uses Apache http client.
 *
 * @author Jens Wilke
 */
public class GitHubStatisticsJava11Source implements AsyncCacheLoader<String, GitHubCounters> {

  private HttpClient client = HttpClient.newHttpClient();

  public void load(String key, Context<String, GitHubCounters> context, final Callback<GitHubCounters> callback) {
      request(key).whenComplete((v, t) -> {
        if (t != null)
          callback.onLoadFailure(t);
        else
          callback.onLoadSuccess(v);
      });
  }

  public CompletableFuture<GitHubCounters> request(String key) {
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create("https://api.github.com/repos/" + key))
      .build();
    return client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
      .thenApply(HttpResponse::body)
      .thenApply(GitHubStatisticsSource::parse);
  }

}
