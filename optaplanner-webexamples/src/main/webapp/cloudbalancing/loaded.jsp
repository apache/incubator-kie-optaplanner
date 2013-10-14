<%@ page import="org.optaplanner.webexamples.cloudbalancing.CloudWebAction" %>
<%
  new CloudWebAction().setup(session);
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Drools Planner webexamples: Cloud Balancing</title>
  <link href="<%=application.getContextPath()%>/twitterbootstrap/css/bootstrap.css" rel="stylesheet">
  <link href="<%=application.getContextPath()%>/twitterbootstrap/css/bootstrap-responsive.css" rel="stylesheet">
  <link href="<%=application.getContextPath()%>/website/css/droolsPlannerWebexamples.css" rel="stylesheet">
  <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
  <!--[if lt IE 9]>
  <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
  <![endif]-->
</head>
<body>

<div class="container-fluid">
<div class="row-fluid">
<div class="span2">
  <div class="benchmark-report-nav">
    <a href="http://www.optaplanner.org/"><img src="../website/img/optaPlannerLogo.png" alt="Drools Planner"/></a>
  </div>
</div>
<div class="span10">
  <header class="main-page-header">
    <h1>Cloud Balancing</h1>
  </header>
  <p>Balance processes over Cloud Resources</p>
  <p>A dataset has been loaded.</p>
  <div>
    <button class="btn" onclick="window.location.href='solve.jsp'"><i class="icon-play"></i> Solve this planning problem</button>
  </div>
  <img src="showSchedule.png"/>
</div>
</div>
</div>

<script src="<%=application.getContextPath()%>/twitterbootstrap/js/jquery.js"></script>
<script src="<%=application.getContextPath()%>/twitterbootstrap/js/bootstrap.js"></script>
</body>
</html>
