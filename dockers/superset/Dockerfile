ARG SUPERSET_VERSION=2.1.0
FROM docker.io/apache/superset:${SUPERSET_VERSION}

USER root

RUN pip install --no-cache -e '.[postgres,trino,redshift,mysql,bigquery,gsheets,athena,clickhouse,cors,excel,druid,mysql,redshift]'\
  && pip install --no-cache 'flask-appbuilder[oauth]' duckdb-engine \
  && pip install --no-cache --upgrade 'clickhouse-connect>=0.5.14, <1.0' \
  && pip install --no-cache https://github.com/acryldata/PyHive/archive/refs/heads/master.zip


USER superset