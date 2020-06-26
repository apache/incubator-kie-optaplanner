const colors = [
  'aquamarine',
  'blueviolet',
  'chocolate',
  'cornflowerblue',
  'crimson',
  'forestgreen',
  'gold',
  'lawngreen',
  'orange',
  'tomato',
];

const colorById = (i) => colors[i % colors.length];
const colorByDemandPoint = (dp) => dp.facility === null ? {} : { color: colorById(dp.facility.id) };

const get = () => {
  fetch('http://localhost:8080/flp/get')
    .then(response => response.json())
    .then(data => showProblem(data));
};

const showProblem = (problem) => {
  map.fitBounds(problem.bounds);
  problem.facilities.forEach((facility) => L.marker(facility.location).addTo(map));
  problem.demandPoints.forEach((dp) => {
    const color = colorByDemandPoint(dp);
    L.circleMarker(dp.location, color).addTo(map);
    if (dp.facility !== null) {
      L.polyline([dp.location, dp.facility.location], color).addTo(map);
    }
  });
};

const map = L.map('map').setView([51.505, -0.09], 13);

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  maxZoom: 19,
  attribution: '&copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors',
}).addTo(map);

map.on('click', get);
