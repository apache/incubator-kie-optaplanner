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

const update = () => {
  fetch('/flp/solution')
    .then(response => response.json())
    .then(data => showProblem(data));
};

const solve = () => {
  fetch('/flp/solve', { method: 'POST' });
};

const facilityPopupContent = (f) => `<ul>
<li>Usage: ${f.usage}/${f.capacity}</li>
<li>Setup cost: ${f.setupCost}</li>
</ul>`;

const showProblem = (problem) => {
  markerGroup.clearLayers();
  map.fitBounds(problem.bounds);
  problem.facilities.forEach((facility) =>
    L.marker(facility.location)
      .addTo(markerGroup)
      .bindPopup(facilityPopupContent(facility)),
  );
  problem.demandPoints.forEach((dp) => {
    const color = colorByDemandPoint(dp);
    L.circleMarker(dp.location, color).addTo(markerGroup);
    if (dp.facility !== null) {
      L.polyline([dp.location, dp.facility.location], color).addTo(markerGroup);
    }
  });
};

const map = L.map('map', { doubleClickZoom: false }).setView([51.505, -0.09], 13);
const markerGroup = L.layerGroup();

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  maxZoom: 19,
  attribution: '&copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors',
}).addTo(map);

markerGroup.addTo(map);

map.whenReady(update);
