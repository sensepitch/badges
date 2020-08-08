package com.headissue.badges;

import org.cache2k.Cache2kBuilder;
import org.cache2k.KeyValueSource;
import org.cache2k.integration.CacheLoader;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * The main servlet. Additional badges can be added by adding an additional data
 * source and a jsp template.
 *
 * @author Jens Wilke
 */
public class QueryServlet extends HttpServlet {

  final static int REFRESH_MINUTES = 27;
  final static int EDGE_MAX_AGE_SECONDS = REFRESH_MINUTES * 60 / 2;

  Map<String, KeyValueSource<String, ?>> counterSources = new HashMap<String, KeyValueSource<String, ?>>();

  {
    addSrouce("github", new GitHubStatisticsSource());
    addSrouce("maven", new MavenCentralSource());
  }

  @SuppressWarnings("unchecked")
  void addSrouce(String name, CacheLoader<String, ?> l) {
    counterSources.put(name,
      Cache2kBuilder.of(String.class, Object.class)
        .name(name)
        .loader((CacheLoader<String, Object>) l)
        .refreshAhead(true)
        .expireAfterWrite(REFRESH_MINUTES, TimeUnit.MINUTES)
        .build());
  }

  /**
   * {@code called with, e.g.: /img/github/starGazers/gh-stargazers/cache2k/cache2k}
   */
  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    resp.setHeader("Cache-Control", "public, max-age=" + EDGE_MAX_AGE_SECONDS);
    String pi = req.getPathInfo();
    if (pi == null) {
      throw new InvalidException();
    }
    String[] params = pi.split("/");
    if (params.length < 4) {
      throw new InvalidException();
    }
    String sourceName = params[1];
    String metricName = params[2];
    String templateName = params[3];
    int idx = pi.substring(0, pi.lastIndexOf('/')).lastIndexOf('/');
    String project = pi.substring(idx + 1);
    Object result = counterSources.get(sourceName).get(project);
    Object data;
    try {
      Method m = result.getClass().getMethod("get" + Character.toUpperCase(metricName.charAt(0)) + metricName.substring(1));
      data = m.invoke(result);
    } catch (NoSuchMethodException x) {
     throw new InvalidException();
    } catch (IllegalAccessException x) {
      throw new InvalidException();
    } catch (InvocationTargetException x) {
      throw new InvalidException();
    }
    RequestDispatcher dp = req.getRequestDispatcher("/" + templateName + ".jsp?project=" + project + "&data=" + URLEncoder.encode(data.toString(), "UTF-8"));
    dp.forward(req, resp);
  }

}
