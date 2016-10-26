# General course assignment

Build a map-based application, which lets the user see geo-based data on a map and filter/search through it in a meaningfull way. Specify the details and build it in your language of choice. The application should have 3 components:

1. Custom-styled background map, ideally built with [mapbox](http://mapbox.com). Hard-core mode: you can also serve the map tiles yourself using [mapnik](http://mapnik.org/) or similar tool.
2. Local server with [PostGIS](http://postgis.net/) and an API layer that exposes data in a [geojson format](http://geojson.org/).
3. The user-facing application (web, android, ios, your choice..) which calls the API and lets the user see and navigate in the map and shows the geodata. You can (and should) use existing components, such as the Mapbox SDK, or [Leaflet](http://leafletjs.com/).

## Example projects

- Showing nearby landmarks as colored circles, each type of landmark has different circle color and the more interesting the landmark is, the bigger the circle. Landmarks are sorted in a sidebar by distance to the user. It is possible to filter only certain landmark types (e.g., castles).

- Showing bicykle roads on a map. The roads are color-coded based on the road difficulty. The user can see various lists which help her choose an appropriate road, e.g. roads that cross a river, roads that are nearby lakes, roads that pass through multiple countries, etc.

## Data sources

- [Open Street Maps](https://www.openstreetmap.org/)

## My project

Fill in (either in English, or in Slovak):

**Application description**:

The application shows waterways and waterareas in the west of Slovakia. Application allows filtering by waterway / waterarea type and by area.

**Data source**:

Custom extract (west of Slovakia) was downloaded from [Mapzen](https://mapzen.com/data/metro-extracts) in .geojson format.
Only files mapping waterareas and waterways were used. Import was done by ogr2ogr tool.


**Technologies used**:

[Mapbox](https://www.mapbox.com/) is used to serve map tiles.
[Leaflet](http://leafletjs.com/) is used as a to display interactive map in the front-end client.
[PostgreSQL](https://www.postgresql.org/) with [PostGIS](http://www.postgis.net/) extension is used an a database engine.
[Dropwizard](http://www.dropwizard.io/0.9.0/docs/) is used as a framework for server application.

