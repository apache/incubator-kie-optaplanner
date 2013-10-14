<%@ page import="org.optaplanner.webexamples.cloudbalancing.CloudWebAction, java.util.Hashtable, java.util.Iterator, java.util.Set, java.util.Map, java.util.TreeMap" %>
<%
  TreeMap tm = new TreeMap();
  //Hashtable bestSolution;
  CloudWebAction cloudwebaction = new CloudWebAction();
  //bestSolution = (Hashtable) cloudwebaction.toDisplayString(session);
  tm = (TreeMap) cloudwebaction.toDisplayString(session);
  String key;
  String[ ] valueArray= new String[9];
  String value;

// TreeMap tm = new TreeMap();
// tm.putAll(bestSolution);
%>

<!DOCTYPE html>
<html lang="en">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Drools Planner webexamples: vehicle routing</title>
  <link href="<%=application.getContextPath()%>/twitterbootstrap/css/bootstrap.css" rel="stylesheet">
  <link href="<%=application.getContextPath()%>/twitterbootstrap/css/bootstrap-responsive.css" rel="stylesheet">
  <link href="<%=application.getContextPath()%>/website/css/droolsPlannerWebexamples.css" rel="stylesheet">
  <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
  <!--[if lt IE 9]>
  <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
  <![endif]-->

  <!-- HACK to refresh this page automatically every 2 seconds -->
  <!-- TODO: it should only refresh the image -->
  <meta http-equiv="REFRESH" content="2;url=<%=application.getContextPath()%>/cloudbalancing/solving.jsp"/>
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
  <p>Balance processes over Cloud Resources.</p>
  <p>Solving... Below is a temporary solution, refreshed every 2 seconds.</p>
  <div>
    <button class="btn" onclick="window.location.href='<%=application.getContextPath()%>/cloudbalancing/terminateEarly.jsp'"><i class="icon-stop"></i> Terminate early</button>
  </div>

<table border='1'>
<caption>Cloud Balance</caption>
<thead> <tr> <th></th> <th>Computer Name</th> <th>Computer Resources</th> <th>Process Name</th> <th>Process Resources</th> </tr> </thead>
<tfoot> <tr> <td></td> </tr> </tfoot>
<tbody>
<%
//Enumeration k = bestSolution.keys();
//while (k.hasMoreElements()) {
//	key = (String) k.nextElement();
//        valueArray = (String[ ]) bestSolution.get(key); 

//Set set = tm.entrySet();
//Iterator i = set.iterator();
//while(i.hasNext()) {
//	Map.Entry me = (Map.Entry)i.next();
//	key = (String) me.getKey();
// valueArray = (String[ ]) me.getValue()

//   for (key : tm.KeySet() {
//	valueArray = (String[ ]) tm.get(key);

Set keys = tm.keySet();
for (Iterator i = keys.iterator(); i.hasNext();) {
	key = (String) i.next();
	valueArray = (String[ ]) tm.get(key);


%>
<tr>  
   <th rowspan '4'> 
		<img src='../website/img/blades.png' alt='My Physical Machine' height=15% width=15%>
   </th>
   <th rowspan='4'>
 	<%= valueArray[0]  %>
   </th>
   <td>
      CPU : <%= valueArray[1] %> 
   </td>
   <th rowspan='4'>
	<%= valueArray[8]  %>  
   </th>
   <td>
      CPU : <%= valueArray[5] %>
   </td>
</tr>
<tr>
   <td>
      RAM : <%= valueArray[2] %>                 
   </td>
   <td>
      MEM : <%= valueArray[6] %>
   </td>
</tr>
<tr>
   <td>
      NET : <%= valueArray[3] %>                 
   </td>
   <td>
      NET : <%= valueArray[7] %>
   </td>
</tr>
<tr>
   <td>
      $ : <%= valueArray[4] %>
   </td>
   <td>
      ID : <%= key %>
   </td>
</tr>

<%
//System.out.println(" +++++++++++ " + valueArray[0] + " -> CPU : " + valueArray[1]);
valueArray = null;
}
%>
</tbody>
</table>
</div>
</div>
</div>

<script src="<%=application.getContextPath()%>/twitterbootstrap/js/jquery.js"></script>
<script src="<%=application.getContextPath()%>/twitterbootstrap/js/bootstrap.js"></script>
</body>
</html>
