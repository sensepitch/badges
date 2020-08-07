<%--suppress ALL --%><%
    String project = request.getParameter("project");
    if (!project.matches("[-a-z0-9/]*")) {
      throw new IllegalArgumentException("wrong chars in project name");
    }
    String count = request.getParameter("data");

    int counterWidth = 7 + 6 * count.length();
    int totalWidth = 84 - 27 + counterWidth;

    response.setContentType("image/svg+xml");

%><?xml version="1.0"?>
<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="<%= totalWidth %>" height="20">
  <style>#llink:hover{fill:url(#b);stroke:#ccc}#rlink:hover{fill:#4183c4}</style>
  <linearGradient id="a" x2="0" y2="100%">
    <stop offset="0" stop-color="#fcfcfc" stop-opacity="0"/>
    <stop offset="1" stop-opacity=".1"/>
  </linearGradient>
  <linearGradient id="b" x2="0" y2="100%">
    <stop offset="0" stop-color="#ccc" stop-opacity=".1"/>
    <stop offset="1" stop-opacity=".1"/>
  </linearGradient>
  <g stroke="#d5d5d5">
    <rect stroke="none" fill="#fcfcfc" x=".5" y=".5" width="50" height="19" rx="2"/>
    <rect y=".5" x="56.5" width="<%= counterWidth %>" height="19" rx="2" fill="#fafafa"/>
    <path stroke="#fafafa" d="M56 7.5h.5v5H56z"/>
    <path d="M56.5 6.5l-3 3v1l3 3" stroke="d5d5d5" fill="#fafafa"/>
  </g>
  <image x="5" y="3" width="14" height="14" xlink:href="data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0MCIgaGVpZ2h0PSI0MCIgdmlld0JveD0iMTIgMTIgNDAgNDAiPgo8cGF0aCBmaWxsPSIjMzMzMzMzIiBkPSJNMzIsMTMuNGMtMTAuNSwwLTE5LDguNS0xOSwxOWMwLDguNCw1LjUsMTUuNSwxMywxOGMxLDAuMiwxLjMtMC40LDEuMy0wLjljMC0wLjUsMC0xLjcsMC0zLjIgYy01LjMsMS4xLTYuNC0yLjYtNi40LTIuNkMyMCw0MS42LDE4LjgsNDEsMTguOCw0MWMtMS43LTEuMiwwLjEtMS4xLDAuMS0xLjFjMS45LDAuMSwyLjksMiwyLjksMmMxLjcsMi45LDQuNSwyLjEsNS41LDEuNiBjMC4yLTEuMiwwLjctMi4xLDEuMi0yLjZjLTQuMi0wLjUtOC43LTIuMS04LjctOS40YzAtMi4xLDAuNy0zLjcsMi01LjFjLTAuMi0wLjUtMC44LTIuNCwwLjItNWMwLDAsMS42LTAuNSw1LjIsMiBjMS41LTAuNCwzLjEtMC43LDQuOC0wLjdjMS42LDAsMy4zLDAuMiw0LjcsMC43YzMuNi0yLjQsNS4yLTIsNS4yLTJjMSwyLjYsMC40LDQuNiwwLjIsNWMxLjIsMS4zLDIsMywyLDUuMWMwLDcuMy00LjUsOC45LTguNyw5LjQgYzAuNywwLjYsMS4zLDEuNywxLjMsMy41YzAsMi42LDAsNC42LDAsNS4yYzAsMC41LDAuNCwxLjEsMS4zLDAuOWM3LjUtMi42LDEzLTkuNywxMy0xOC4xQzUxLDIxLjksNDIuNSwxMy40LDMyLDEzLjR6Ii8+Cjwvc3ZnPgo="/>
  <g fill="#333" text-anchor="middle" font-family="Helvetica Neue,Helvetica,Arial,sans-serif" font-weight="700" font-size="11">
    <text x="33.5" y="15" fill="#fff">Star</text>
    <text x="33.5" y="14">Star</text>
    <text x="<%= 69 - 12 + counterWidth / 2 %>" y="15" fill="#fff"><%= count %></text>
    <a xlink:href="https://github.com/<%= project %>/stargazers">
      <text id="rlink" x="<%= 69 - 12 + counterWidth / 2 %>" y="14"><%= count %></text>
    </a>
  </g>
  <a xlink:href="https://github.com/<%= project %>">
    <rect id="llink" stroke="#d5d5d5" fill="url(#a)" x=".5" y=".5" width="50" height="19" rx="2"/>
  </a>
</svg>
