package com.headissue.badges;

/**
 * Java object representation of GitHub counters
 *
 * @author Jens Wilke
 */
public class GitHubCounters {

  private int starGazers;
  private int forks;
  private int subscribers;

  public int getStarGazers() {
    return starGazers;
  }

  public void setStarGazers(final int v) {
    starGazers = v;
  }

  public int getForks() {
    return forks;
  }

  public void setForks(final int v) {
    forks = v;
  }

  public int getSubscribers() {
    return subscribers;
  }

  public void setSubscribers(final int v) {
    subscribers = v;
  }

}
