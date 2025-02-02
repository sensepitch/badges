package com.headissue.badges;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * Render arbitrary colored text in badges style
 *
 * @author Jens Wilke
 */
public class TextBadgeServlet extends HttpServlet {

  /**
   * {@code called with, e.g.: txt/hello/world}
   */
  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    String pi = req.getPathInfo();
    if (pi == null) {
      throw new InvalidException();
    }
    String[] params = pi.split("/");
    if (params.length < 2) {
      throw new InvalidException();
    }
    String left = params[1];
    String right = params[2];
    String leftColor = "555";
    String rightColor = "4c2";
    int idx = left.lastIndexOf(",");
    if (idx >= 0) {
      leftColor = left.substring(idx + 1);
      left = left.substring(0, idx);
    }
    idx = right.lastIndexOf(",");
    if (idx >= 0) {
      rightColor = right.substring(idx + 1);
      right = right.substring(0, idx);
    }
    RequestDispatcher dp = req.getRequestDispatcher("/left-right.jsp?"
      + "left=" + URLEncoder.encode(left, "UTF-8")
      + "&right=" + URLEncoder.encode(right, "UTF-8")
      + "&leftColor=" + URLEncoder.encode(leftColor, "UTF-8")
      + "&rightColor=" + URLEncoder.encode(rightColor, "UTF-8"));
    dp.forward(req, resp);
  }

}
