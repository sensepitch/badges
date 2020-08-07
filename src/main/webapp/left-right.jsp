<%--suppress ALL --%><%@ page import="java.awt.geom.AffineTransform" %><%@
        page import="java.awt.font.FontRenderContext" %><%@
        page import="java.awt.*" %><%!

  static String fmt(double v) {
    long l = (long) (v * 10);
    if (l % 10 == 0) {
      return Long.toString(l / 10);
    }
    return String.format("%.1f", v);
  }

  static double estimateWidth(String txt) {
    AffineTransform affinetransform = new AffineTransform();
    FontRenderContext frc = new FontRenderContext(affinetransform,true,true);
    Font font = new Font("sans-serif", Font.PLAIN, 11);
    return font.getStringBounds(txt, frc).getWidth();
  }

%><%

    String left = request.getParameter("left");
    if (!left.matches("[-.A-Za-z0-9/]*")) {
      throw new IllegalArgumentException("wrong chars in name");
    }
    String right = request.getParameter("right");
    if (!right.matches("[-.A-Za-z0-9/]*")) {
        throw new IllegalArgumentException("wrong chars in name");
    }
    String colorRight = request.getParameter("rightColor");
    if (colorRight == null) {
      colorRight = "4c1";
    }
    if (!colorRight.matches("[a-z0-9]*")) {
        throw new IllegalArgumentException("wrong chars in color");
    }
    String colorLeft = request.getParameter("leftColor");
    if (colorLeft == null) {
        colorLeft = "555";
    }
    if (!colorLeft.matches("[a-z0-9]*")) {
        throw new IllegalArgumentException("wrong chars in color");
    }

    double leftWidth = estimateWidth(left) * 1.027 + 7;
    double rightWidth = estimateWidth(right) * 1.027 + 7;

    response.setContentType("image/svg+xml");

%><?xml version="1.0"?>
<svg xmlns="http://www.w3.org/2000/svg" width="<%= fmt(leftWidth + rightWidth) %>" height="20">
  <linearGradient id="b" x2="0" y2="100%">
    <stop offset="0" stop-color="#bbb" stop-opacity=".1"/>
    <stop offset="1" stop-opacity=".1"/>
  </linearGradient>
  <clipPath id="a">
    <rect width="<%= fmt(leftWidth + rightWidth) %>" height="20" rx="3" fill="#fff"/>
  </clipPath>
  <g clip-path="url(#a)">
    <path fill="#<%= colorLeft %>" d="M0 0h<%= fmt(leftWidth) %>v20H0z"/>
    <path fill="#<%= colorRight %>" d="M<%= fmt(leftWidth) %> 0h<%= fmt(rightWidth) %>v20H<%= fmt(leftWidth) %>%>z"/>
    <path fill="url(#b)" d="M0 0h<%= fmt(leftWidth + rightWidth) %>v20H0z"/>
  </g>
  <g fill="#fff" text-anchor="middle" font-family="DejaVu Sans,Verdana,Geneva,sans-serif" font-size="11">
    <text x="<%= fmt( 1D * leftWidth / 2) %>" y="15" fill="#010101" fill-opacity=".3"><%= left %></text>
    <text x="<%= fmt( 1D * leftWidth / 2) %>" y="14"><%= left %></text>
    <text x="<%= 1D * rightWidth / 2 + leftWidth %>" y="15" fill="#010101" fill-opacity=".3"><%= right %></text>
    <text x="<%= 1D * rightWidth / 2 + leftWidth %>" y="14"><%= right %></text>
  </g>
</svg>
