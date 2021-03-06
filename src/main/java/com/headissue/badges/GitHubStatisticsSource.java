package com.headissue.badges;

import org.apache.commons.io.IOUtils;
import org.cache2k.integration.CacheLoader;
import org.json.JSONObject;


import java.io.InputStream;
import java.net.URL;

/**
 * Request counters from GitHub.
 *
 * @author Jens Wilke
 */
public class GitHubStatisticsSource extends CacheLoader<String, GitHubCounters> {

  public GitHubCounters load(final String key) throws Exception {
    InputStream in = new URL("https://api.github.com/repos/" + key).openConnection().getInputStream();
    return parse(in);
  }


  public static GitHubCounters parse(InputStream in) {
    try {
      String theString = IOUtils.toString(in, "UTF-8");
      in.close();
      JSONObject obj = new JSONObject(theString);
      GitHubCounters counters = new GitHubCounters();
      counters.setForks(obj.getInt("forks_count"));
      counters.setStarGazers(obj.getInt("stargazers_count"));
      counters.setSubscribers(obj.getInt("subscribers_count"));
      return counters;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

}
