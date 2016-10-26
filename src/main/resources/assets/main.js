var coordsFIIT = [48.153517, 17.072219];

var map = L.map('map').setView(coordsFIIT, 13);

L.tileLayer('https://api.mapbox.com/styles/v1/mapbox/outdoors-v10/tiles/256/{z}/{x}/{y}?'
          + 'access_token=pk.eyJ1IjoianVyYWptYXR1cyIsImEiOiJjaXVqdXB1ZWwwMDB3MnpxeTJya3hkNW5mIn0.uWZYCVVya3ibb9vCg19PLQ', {
    maxZoom: 18
}).addTo(map);