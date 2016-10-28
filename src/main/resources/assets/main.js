var coordsFIIT = [48.153517, 17.072219];
var map = L.map('map').setView(coordsFIIT, 13);

var setPosition = function(position) {
   map.setView([position.coords.latitude, position.coords.longitude], 13);
};

if ('geolocation' in navigator) {
   navigator.geolocation.getCurrentPosition(setPosition, function(e) {
      console.log(e);
   });
}

var getRect = function() {
   var topLeft = map.containerPointToLatLng([0, 0]);
   var bottomRight = map.containerPointToLatLng(map.getSize());
   return {
      point1: topLeft,
      point2: bottomRight
   };
};

var makeQuery = function(suffix) {
   
   var running = false;
   
   return function(data, handler) {
      if (running) {
         return;
      }
      
      running = true;
      
      $.ajax({
         type: 'POST',
         url: '/service/query' + suffix,
         contentType: 'application/json',
         data: JSON.stringify(data),
         success: handler,
         complete: function() {
            running = false;
         }
      });
   };
   
};

var queryRect = makeQuery('');
var queryClosest = makeQuery('/closest');

var Markers = function(color, weight) {
   var lastMarkers = false;
   
   return {
      add: function(data) {
         var markers = L.geoJSON(data, {
            color: color,
            weight: weight,
            opacity: 0.6,
            onEachFeature: function(feature, layer) {
               layer.bindPopup(feature.properties.name || 'Untitled ' + (feature.properties.type || ''));
            }
         });
         this.remove();
         markers.addTo(map);
         lastMarkers = markers;
      },
      remove: function() {
         if (lastMarkers) {
            lastMarkers.removeFrom(map);
         }
      }
   };
};

var rectMarkers = new Markers('#9999cc', 5);
var closestMarkers = new Markers('#33cc33', 10);

var isSame = function(feature1, feature2) {
   return feature1.id === feature2.id
         || (feature1.properties.name !== ''
            && feature1.properties.name === feature2.properties.name);
};

var refresh = function() {
   var types = $('#filter-types-active').prop('checked')
               ? $.makeArray($('[name="filter-type"]').filter(':checked').map(function() {
                  return $(this).val();
               }))
               : null;
               
   var areaRange = $('#filter-area-active').prop('checked')
               ? $.map($('[name="filter-area"]').val().split(","), parseFloat)
               : false;
   
   queryClosest(map.getCenter(), function(closestData) {
      queryRect({
         rectangle: getRect(),
         types: types,
         areaFrom: areaRange ? areaRange[0] : null,
         areaTo: areaRange ? areaRange[1] : null
      }, function(rectData) {
         var _closestData = [];
         var _rectData = [];
         $.each(rectData, function(j, feature) {
            for (var i = 0; i < closestData.length; i++) {
               if (isSame(feature, closestData[i])) {
                  _closestData.push(feature);
                  return;
               }
            }
            _rectData.push(feature);
         });
         $.each(closestData, function(j, feature) {
            for (var i = 0; i < _closestData.length; i++) {
               if (isSame(feature, _closestData[i])) {
                  return;
               }
            }
            _closestData.push(feature);
         });
         rectMarkers.add(_rectData);
         closestMarkers.add(_closestData);
      });
   });
};

var initFilters = function() {
   $.ajax({
      type: 'GET',
      url: 'service/stats/all',
      success: function(data) {
         var types = data.types;
         if ($.isArray(types)) {
            var container = $('#filter-types-entries');
            container.children().remove();
            $.each(types, function(i, type) {
               var input = $('<input type="checkbox" name="filter-type" />')
                              .attr('value', type);
               var label = $('<label />').text(type).prepend(input);
               var div = $('<div />').append(label).appendTo(container);
            });
         }
         
         var area = $('[name="filter-area"]');
         var vals = [0, 0];
         if (typeof data.minArea === 'number') {
            area.attr('min', data.minArea);
            vals[0] = data.minArea;
         }
         if (typeof data.maxArea === 'number') {
            area.attr('max', data.maxArea);
            vals[1] = data.maxArea;
         }
         multirange(area[0]);
         area.val(vals.join(","));
         
         $('input').on('change', refresh);
      }
   });
};
initFilters();

map.on('zoomlevelschange zoomend move', refresh);

L.tileLayer('https://api.mapbox.com/styles/v1/mapbox/outdoors-v10/tiles/256/{z}/{x}/{y}?'
          + 'access_token=pk.eyJ1IjoianVyYWptYXR1cyIsImEiOiJjaXVqdXB1ZWwwMDB3MnpxeTJya3hkNW5mIn0.uWZYCVVya3ibb9vCg19PLQ', {
    maxZoom: 18
}).addTo(map);