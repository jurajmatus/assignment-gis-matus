var coordsFIIT = [48.153517, 17.072219];

var map = L.map('map').setView(coordsFIIT, 13);

var getRect = function() {
   var topLeft = map.containerPointToLatLng([0, 0]);
   var bottomRight = map.containerPointToLatLng(map.getSize());
   return [topLeft, bottomRight];
};

var query = function(handler, point1, point2) {
   $.ajax({
      type: 'POST',
      url: '/service/waterways/in-rectangle',
      contentType: 'application/json',
      data: JSON.stringify({
         point1: point1,
         point2: point2
      }),
      success: handler
   });
}

var queryCurrent = function(handler) {
   query.apply(window, [handler].concat(getRect()));
};

map.on('zoomlevelschange zoomend move', function(e) {
   console.log(e.type);
});

L.tileLayer('https://api.mapbox.com/styles/v1/mapbox/outdoors-v10/tiles/256/{z}/{x}/{y}?'
          + 'access_token=pk.eyJ1IjoianVyYWptYXR1cyIsImEiOiJjaXVqdXB1ZWwwMDB3MnpxeTJya3hkNW5mIn0.uWZYCVVya3ibb9vCg19PLQ', {
    maxZoom: 18
}).addTo(map);