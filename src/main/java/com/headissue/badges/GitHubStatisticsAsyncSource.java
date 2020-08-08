package com.headissue.badges;

import org.apache.commons.io.IOUtils;
import org.cache2k.integration.AsyncCacheLoader;
import org.cache2k.integration.CacheLoader;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;

/**
 * Request counters from GitHub.
 *
 * @author Jens Wilke
 */
public class GitHubStatisticsAsyncSource implements AsyncCacheLoader<String, GitHubCounters> {

  public void load(final String key, final Context<String, GitHubCounters> context, final Callback<GitHubCounters> callback) {
    context.getExecutor().execute(new Runnable() {
      public void run() {
        try {
          callback.onLoadSuccess(load(key));
        } catch (Throwable t) {
          callback.onLoadFailure(t);
        }
      }
    });
  }

  public GitHubCounters load(final String key) throws Exception {
    InputStream in = new URL("https://api.github.com/repos/" + key).openConnection().getInputStream();
    String theString = IOUtils.toString(in, "UTF-8");
    in.close();
    JSONObject obj = new JSONObject(theString);
    GitHubCounters counters = new GitHubCounters();
    counters.setForks(obj.getInt("forks_count"));
    counters.setStarGazers(obj.getInt("stargazers_count"));
    counters.setSubscribers(obj.getInt("subscribers_count"));
    return counters;
  }

}
