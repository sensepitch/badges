package com.headissue.badges;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.cache2k.integration.AsyncCacheLoader;

/**
 * Request counters from GitHub. Async version which uses Apache http client.
 *
 * @author Jens Wilke
 */
public class GitHubStatisticsAsyncHttpClientSource implements AsyncCacheLoader<String, GitHubCounters> {

  final CloseableHttpAsyncClient httpClient = HttpAsyncClients.createDefault();

  {
    httpClient.start();
  }

  public void load(final String key, final Context<String, GitHubCounters> context, final Callback<GitHubCounters> callback) {
    final HttpGet req = new HttpGet("https://api.github.com/repos/" + key);
    httpClient.execute(req, new FutureCallback<>() {
      @Override
      public void completed(final HttpResponse httpResponse) {
        try {
          HttpEntity ent = httpResponse.getEntity();
          callback.onLoadSuccess(GitHubStatisticsSource.parse(ent.getContent()));
        } catch (Exception ex) {
          failed(ex);
        }
      }

      @Override
      public void failed(final Exception e) {
        callback.onLoadFailure(e);
      }

      @Override
      public void cancelled() {
        callback.onLoadFailure(new RuntimeException());
      }
    });
  }

}
