package com.headissue.badges;

import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.CacheOperationCompletionListener;
import org.cache2k.integration.AsyncCacheLoader;
import org.cache2k.integration.CacheLoader;

import javax.servlet.AsyncContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * The main servlet. Additional badges can be added by adding an additional data
 * source and a jsp template.
 *
 * @author Jens Wilke
 */
public class AsyncQueryServlet extends HttpServlet {

  final static int REFRESH_MINUTES = 27;
  final static int EDGE_MAX_AGE_SECONDS = REFRESH_MINUTES * 60 / 2;

  Map<String, Cache<String, ?>> counterSources = new HashMap<String, Cache<String, ?>>();

  {
    addSource("github", new GitHubStatisticsJava11Source());
    addSource("maven", new MavenCentralSource());
  }

  @SuppressWarnings("unchecked")
  void addSource(String name, CacheLoader<String, ?> l) {
    counterSources.put(name,
      Cache2kBuilder.of(String.class, Object.class)
        .name(name)
        .loader((CacheLoader<String, Object>) l)
        .refreshAhead(true)
        .expireAfterWrite(REFRESH_MINUTES, TimeUnit.MINUTES)
        .build());
  }

  void addSource(String name, AsyncCacheLoader<String, ?> l) {
    counterSources.put(name,
      Cache2kBuilder.of(String.class, Object.class)
        .name(name)
        .loader((AsyncCacheLoader<String, Object>) l)
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
    Cache<String, ?> c = counterSources.get(sourceName);
    // don't go the async path if the value is cached
    // c.peek() does return instantly with the cached value or null
    Object result = c.peek(project);
    if (result != null) {
      processResult(req, resp, metricName, templateName, project, result);
    } else {
      final AsyncContext ac = req.startAsync();
      c.loadAll(new HashSet<>() {
        {
          add(project);
        }
      }, new CacheOperationCompletionListener() {
        @Override
        public void onCompleted() {
          try {
            // loadAll() does not deliver aus the actually loaded values so we need to
            // retrieve it from the cace
            Object r2 = c.get(project);
            Object data = extractMetric(metricName, r2);
            ac.dispatch(dispatchTarget(data, templateName, project));
          } catch (Exception e) {
            onException(e);
          }
        }
        @Override
        public void onException(final Throwable exception) {
          try {
            resp.sendError(500, exception.toString());
          } catch (IOException ignore) { }
        }
      });
    }
  }

  private String dispatchTarget(final Object data, final String templateName, final String project) throws UnsupportedEncodingException {
    return "/" + templateName + ".jsp?project=" + project + "&data=" + URLEncoder.encode(data.toString(), "UTF-8");
  }

  private void processResult(final HttpServletRequest req, final HttpServletResponse resp, final String metricName, final String templateName, final String project, final Object result) throws ServletException, IOException {
    Object data = extractMetric(metricName, result);
    RequestDispatcher dp = req.getRequestDispatcher(dispatchTarget(data, templateName, project));
    dp.forward(req, resp);
  }

  private Object extractMetric(final String metricName, final Object result) {
    Object data;
    try {
      Method m = result.getClass().getMethod("get" + Character.toUpperCase(metricName.charAt(0)) + metricName.substring(1));
      data = m.invoke(result);
    } catch (Exception x) {
     throw new InvalidException();
    }
    return data;
  }

}
