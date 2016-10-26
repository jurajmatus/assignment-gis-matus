#!/bin/bash
sudo -i
su - postgres
createuser gisuser
createdb -O gisuser postgis_template -E UTF-8
createlang plpgsql postgis_template
psql -U postgres -d postgis_template -f /usr/share/postgresql/contrib/postgis-2.3/postgis.sql
psql -U postgres -d postgis_template -f /usr/share/postgresql/contrib/postgis-2.3/spatial_ref_sys.sql
echo "UPDATE pg_database SET datistemplate = TRUE WHERE datname = 'postgis_template';" | psql -U postgres
createdb -T postgis_template project
