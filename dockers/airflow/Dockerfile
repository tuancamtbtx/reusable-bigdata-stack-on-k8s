FROM python:3.8-slim-buster
# Finally install the requirements from the latest sources
# https://github.com/druid-io/pydruid/issues/129
RUN pip install pytest-runner

RUN apt-get update\
    && apt-get install -y --no-install-recommends htop git default-libmysqlclient-dev libsnappy-dev liblzma-dev patch curl default-mysql-client\
    && apt-get autoremove -yqq --purge \
    && curl -fsSLo /usr/bin/kubectl "https://dl.k8s.io/release/v1.20.4/bin/linux/amd64/kubectl"\
    && chmod +x /usr/bin/kubectl\
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*
# set default timezone
RUN rm -f /etc/localtime && ln -s /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime

RUN adduser airflow

COPY ./requirements_airflow.txt  constraints.txt /tmp/

ARG DEV_APT_DEPS="\
    build-essential"
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
    ${DEV_APT_DEPS} \
    ${ADDITIONAL_DEV_APT_DEPS} \
    && pip install --upgrade pip \
    && pip install --use-deprecated=legacy-resolver --no-cache-dir -r /tmp/requirements_airflow.txt --constraint "/tmp/constraints.txt" \
    && rm -f /tmp/requirements_airflow.txt /tmp/constraints.txt\
    && apt-get purge ${DEV_APT_DEPS} -y \
    && apt-get autoremove -y \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

ENV TINI_VERSION v0.19.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini-static /usr/bin/tini
RUN chmod +x /usr/bin/tini

ENV AIRFLOW_HOME=/home/airflow/airflow

RUN mkdir -p $AIRFLOW_HOME
RUN chown airflow:airflow $AIRFLOW_HOME
ENTRYPOINT ["tini", "--"]