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
public class MavenCentralSource extends CacheLoader<String, MavenCentralContent> {

  private final String sanitize(String v) {
    final int _MAX_LEN = 50;
    if (v.length() > _MAX_LEN) {
      v = v.substring(0, _MAX_LEN);
    }
    StringBuilder sb = new StringBuilder();
    for (char c : v.toCharArray()) {
      if (Character.isLowerCase(c) || Character.isDigit(c)
        || ".-".indexOf(c) >= 0) {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  public MavenCentralContent load(final String key) throws Exception {
    String[] sa = key.split("/");
    String _group = sa[0];
    String _artifact = sa[1];
    /*-
    InputStream in = new URL(
      "https://search.maven.org/solrsearch/select?q=g:\"" + _group + "\"" +
        "+AND+a:\"" + _artifact + "\"&wt=json").openConnection().getInputStream();
        -*/
    InputStream in = new URL(
      "https://search.maven.org/solrsearch/select?q=g:" + sanitize(_group) + "" +
        "+AND+a:" + sanitize(_artifact) + "&wt=json").openConnection().getInputStream();
    String s = IOUtils.toString(in, "UTF-8");
    in.close();
    JSONObject obj = new JSONObject(s);
    // System.out.println(obj);
    JSONObject _found = obj.getJSONObject("response").getJSONArray("docs").getJSONObject(0);
    MavenCentralContent c = new MavenCentralContent();
    c.setLatestVersion(_found.getString("latestVersion"));
    return c;
  }

}
