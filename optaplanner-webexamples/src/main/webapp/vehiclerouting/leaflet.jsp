<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <title>OptaPlanner webexamples: vehicle routing with leaflet.js</title>
  <link href="<%=application.getContextPath()%>/website/leaflet/leaflet.css" rel="stylesheet">
  <link href="<%=application.getContextPath()%>/vehiclerouting/vehicleRouting.css" rel="stylesheet">
  <jsp:include page="/common/head.jsp"/>
</head>
<body>

<div class="container">
  <div class="row">
    <div class="col-md-3">
      <jsp:include page="/common/menu.jsp"/>
    </div>
    <div class="col-md-9">
      <header class="main-page-header">
        <h1>Vehicle routing</h1>
      </header>
      <a class="btn btn-default active" href="leaflet.jsp">Leaflet.js</a>
      <a class="btn btn-default" href="googleMaps.jsp">Google Maps</a>
      <h2>Leaflet.js visualization</h2>
      <p>Pick up all items of all customers with a few vehicles in the shortest route possible.<br/>
      Each location shows the number of items to pick up. Each vehicle has a limited capacity.</p>
      <p class="pull-right" style="border: solid thin black; border-radius: 5px; padding: 2px;">Total travel distance of vehicles: <b><span id="scoreValue">Not solved</span></b></p>
      <div>
        <button id="solveButton" class="btn btn-default" type="submit" onclick="solve()">Solve this planning problem</button>
        <button id="terminateEarlyButton" class="btn btn-default" type="submit" onclick="terminateEarly()" disabled="disabled">Terminate early</button>
      </div>
      <div id="map" style="height: 600px; margin-top: 10px; margin-bottom: 10px;"></div>
      <p>This visualization can work offline (requires setting up an OpenStreetMap data server) and uses open source software.</p>
    </div>
  </div>
</div>

<jsp:include page="/common/foot.jsp"/>
<script src="<%=application.getContextPath()%>/website/leaflet/leaflet.js"></script>
<script type="text/javascript">
  var map;
  var vehicleRouteLayerGroup;
  var intervalTimer;
var geojsonlayer;
  initMap = function() {
	  map = L.map('map').setView([51, 5], 7);

	  L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoibWFwYm94IiwiYSI6IjZjNmRjNzk3ZmE2MTcwOTEwMGY0MzU3YjUzOWFmNWZhIn0.Y8bhBaUMqFiPrDRW9hieoQ', {
	      maxZoom: 18,
	      attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, ' +
	          '<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ' +
	          'Imagery © <a href="http://mapbox.com">Mapbox</a>',
	      id: 'mapbox.streets'
	  }).addTo(map);

    loadSolution();
//    updateSolution();
  };

  ajaxError = function(jqXHR, textStatus, errorThrown) {
    console.log("Error: " + errorThrown);
    console.log("TextStatus: " + textStatus);
    console.log("jqXHR: " + jqXHR);
    alert("Error: " + errorThrown);
  };

  loadSolution = function() {
      $.ajax({
          url: "<%=application.getContextPath()%>/rest/vehiclerouting/solution/geoJson",
          type: "GET",
          dataType : "json",
          success: function(solution) {
          geojsonlayer = L.geoJson(solution, {
              style : function(feature) {
                  return {color: feature.properties.stroke};
              }
          });
          map.addLayer(geojsonlayer);
          }
        });
  };

  updateSolution = function() {
	    map.removeLayer(geojsonlayer);
	    loadSolution();
  };

  solve = function() {
    $('#solveButton').attr("disabled", "disabled");
    $.ajax({
      url: "<%=application.getContextPath()%>/rest/vehiclerouting/solution/solve",
      type: "POST",
      dataType : "json",
      data : "",
      success: function(message) {
        console.log(message.text);
        intervalTimer = setInterval(function () {
          updateSolution()
        }, 2000);
        $('#terminateEarlyButton').removeAttr("disabled");
      }, error : function(jqXHR, textStatus, errorThrown) {ajaxError(jqXHR, textStatus, errorThrown)}
    });
  };

  terminateEarly = function () {
    $('#terminateEarlyButton').attr("disabled", "disabled");
    window.clearInterval(intervalTimer);
    $.ajax({
      url: "<%=application.getContextPath()%>/rest/vehiclerouting/solution/terminateEarly",
      type: "POST",
      data : "",
      dataType : "json",
      success: function( message ) {
        console.log(message.text);
        updateSolution();
        $('#solveButton').removeAttr("disabled");
      }, error : function(jqXHR, textStatus, errorThrown) {ajaxError(jqXHR, textStatus, errorThrown)}
    });
  };

  initMap();
</script>
</body>
</html>
