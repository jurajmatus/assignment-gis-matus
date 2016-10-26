#!/bin/bash
yaourt -Sa --noconfirm postgresql pgadmin postgis osm2pgsql-git netcdf qgis
echo "
-- verify available extensions\
SELECT name, default_version,installed_version \
FROM pg_available_extensions WHERE name LIKE 'postgis%' ;\
\
-- install extension for spatial database mygisdb\
\c mygisdb\
CREATE EXTENSION postgis;\
CREATE EXTENSION postgis_topology;\
CREATE EXTENSION fuzzystrmatch;\
CREATE EXTENSION postgis_tiger_geocoder;\
" | psql -U postgres
