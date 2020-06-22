const get = () => {
  fetch('http://localhost:8080/flp/get')
    .then(response => response.json())
    .then(data => showProblem(data));
};

const showProblem = (problem) => {
  map.fitBounds(problem.bounds);
  problem.facilities.forEach(facility => L.marker(facility.location).addTo(map));
  problem.demandPoints.forEach(dp => L.circleMarker(dp.location).addTo(map));
  problem.demandPoints
    .filter(dp => dp.facility !== null)
    .forEach(dp => L.polyline([dp.location, dp.facility.location]).addTo(map));
};

const map = L.map('map').setView([51.505, -0.09], 13);

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  maxZoom: 19,
  attribution: '&copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors',
}).addTo(map);

map.on('click', get);
