<%@ page import="org.optaplanner.webexamples.cloudbalancing.CloudWebAction" %>
<%
  new org.optaplanner.webexamples.cloudbalancing.CloudWebAction().terminateEarly(session);
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta http-equiv="REFRESH" content="0;url=<%=application.getContextPath()%>/cloudbalancing/terminated.jsp"/>
</head>
<body>
</body>
</html>
