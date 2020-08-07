package com.headissue.badges;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * @author Jens Wilke
 */
public class TextBadgeServlet extends HttpServlet {

  /**
   * {@code called with, e.g.: /img/github/starGazers/gh-stargazers/cache2k/cache2k}
   */
  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    String pi = req.getPathInfo();
    if (pi == null) {
      throw new InvalidException();
    }
    String[] _params = pi.split("/");
    if (_params.length < 2) {
      throw new InvalidException();
    }
    String _left = _params[1];
    String _right = _params[2];
    String _leftColor = "555";
    String _rightColor = "4c2";
    int idx = _left.lastIndexOf(",");
    if (idx >= 0) {
      _leftColor = _left.substring(idx + 1);
      _left = _left.substring(0, idx);
    }
    idx = _right.lastIndexOf(",");
    if (idx >= 0) {
      _rightColor = _right.substring(idx + 1);
      _right = _right.substring(0, idx);
    }
    RequestDispatcher dp = req.getRequestDispatcher("/left-right.jsp?"
      + "left=" + URLEncoder.encode(_left, "UTF-8")
      + "&right=" + URLEncoder.encode(_right, "UTF-8")
      + "&leftColor=" + URLEncoder.encode(_leftColor, "UTF-8")
      + "&rightColor=" + URLEncoder.encode(_rightColor, "UTF-8"));
    dp.forward(req, resp);
  }

}
