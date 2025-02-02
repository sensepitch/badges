<%@ page import="com.headissue.badges.GcChurnMonitor" %><%@
        page import="java.util.concurrent.atomic.AtomicLong" %><%@
        page contentType="text/html;charset=UTF-8" language="java" %><%!

    final AtomicLong last = new AtomicLong();
    final GcChurnMonitor gcChurnMonitor = new GcChurnMonitor();

%><%

  long bytes = gcChurnMonitor.printChurn();
  long delta = bytes - last.get();
  last.set(bytes);

%><%= delta %>
