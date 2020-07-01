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
let autoRefreshCount = 0;
let autoRefreshIntervalId = null;

const solveButton = $('#solveButton');
const stopSolvingButton = $('#stopSolvingButton');
const facilitiesTable = $('#facilities');

const colorById = (i) => colors[i % colors.length];
const colorByDemandPoint = (dp) => dp.facility === null ? {} : { color: colorById(dp.facility.id) };

const getStatus = () => {
  fetch('/flp/status')
    .then((response) => response.json())
    .then((data) => showProblem(data));
};

const solve = () => {
  fetch('/flp/solve', { method: 'POST' }).then(() => {
    updateSolvingStatus(true);
    autoRefreshCount = 300;
    if (autoRefreshIntervalId == null) {
      autoRefreshIntervalId = setInterval(autoRefresh, 500);
    }
  }).catch((error) => showError('Start solving failed', error));
};

function stopSolving() {
  fetch('/flp/stopSolving', { method: 'POST' }).then(() => {
    updateSolvingStatus(false);
    getStatus();
  }).catch((error) => showError('Stop solving failed', error));
}

function showError(title, reason) {
  console.error(`${title}:`, reason);
}

function updateSolvingStatus(solving) {
  if (solving) {
    solveButton.hide();
    stopSolvingButton.show();
  } else {
    autoRefreshCount = 0;
    solveButton.show();
    stopSolvingButton.hide();
  }
}

function autoRefresh() {
  getStatus();
  autoRefreshCount--;
  if (autoRefreshCount <= 0) {
    clearInterval(autoRefreshIntervalId);
    autoRefreshIntervalId = null;
  }
}

const facilityPopupContent = (facility) => `<h5>Facility ${facility.id}</h5>
<ul class="list-unstyled">
<li>Usage: ${facility.usedCapacity}/${facility.capacity}</li>
<li>Setup cost: ${facility.setupCost}</li>
</ul>`;

const showProblem = ({ solution, isSolving }) => {
  markerGroup.clearLayers();
  map.fitBounds(solution.bounds);
  facilitiesTable.children().remove();
  solution.facilities.forEach((facility) => {
    const { id, location, setupCost, capacity, usedCapacity, used } = facility;
    const percentage = usedCapacity / capacity * 100;
    L.marker(location)
      .addTo(markerGroup)
      .bindPopup(facilityPopupContent(facility));
    facilitiesTable.append(`<tr class="${used ? 'table-active' : 'text-muted'}">
<td>Facility ${id}</td>
<td><div class="progress">
<div class="progress-bar" role="progressbar" style="width: ${percentage}%" aria-valuenow="25" aria-valuemin="0" aria-valuemax="100">${usedCapacity}/${capacity}</div>
</div></td>
<td>$${setupCost}</td>
</tr>`);
  });
  solution.demandPoints.forEach((dp) => {
    const color = colorByDemandPoint(dp);
    L.circleMarker(dp.location, color).addTo(markerGroup);
    if (dp.facility !== null) {
      L.polyline([dp.location, dp.facility.location], color).addTo(markerGroup);
    }
  });
  $('#score').text(`Score: ${solution.score}`);
  updateSolvingStatus(isSolving);
};

const map = L.map('map', { doubleClickZoom: false }).setView([51.505, -0.09], 13);
map.whenReady(getStatus);

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  maxZoom: 19,
  attribution: '&copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors',
}).addTo(map);

const markerGroup = L.layerGroup();
markerGroup.addTo(map);

solveButton.click(solve);
stopSolvingButton.click(stopSolving);

updateSolvingStatus();
