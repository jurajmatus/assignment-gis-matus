#!/bin/bash
cd "$(dirname $0)"
find ../data -type f -iname '*.geojson' | xargs -d '\n' -n 1 -I {} \
   ogr2ogr -f PostgreSQL PG:"host=localhost user=postgres port=5432 dbname=project password=" {} -nln geodata
