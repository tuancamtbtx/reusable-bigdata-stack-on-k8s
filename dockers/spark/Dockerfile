FROM docker.io/eclipse-temurin:11-jre

RUN apt-get update && \
    apt-get install -y netcat procps curl && \
    apt-get autoremove -y && \
    apt-get clean

ARG TINI_VERSION=v0.19.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini

ENV SPARK_HOME /opt/spark
ENV PATH="${SPARK_HOME}/bin:${SPARK_HOME}/sbin:${PATH}"
  
ARG SPARK_VERSION
# must match with hadoop in spark
ARG HADOOP_VERSION=3.3.4
ARG SCALA=2.12
ENV SPARK_VERSION=${SPARK_VERSION:-3.4.1}
ENV SCALA_VERSION=${SCALA}

WORKDIR $SPARK_HOME
RUN set -ex \
  && export HADOOP_MAJOR="$(echo ${HADOOP_VERSION} | grep -Eo '^[0-9]' )" SPARK_SHORT="$(echo ${SPARK_VERSION} | grep -Eo '^[0-9]\.[0-9]')" \
  && curl -fsSL "https://archive.apache.org/dist/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_MAJOR}.tgz" | tar xz --no-same-owner --strip-components=1 -C $SPARK_HOME \
  && mkdir -p $SPARK_HOME/jars/ && cd $SPARK_HOME/jars/ \
  && curl -LO https://repo1.maven.org/maven2/org/apache/spark/spark-avro_${SCALA}/${SPARK_VERSION}/spark-avro_${SCALA}-${SPARK_VERSION}.jar \
  && export AWS_VERSION=1.12.500 \
  && curl -LO https://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk-bundle/${AWS_VERSION}/aws-java-sdk-bundle-${AWS_VERSION}.jar \
  && curl -LO https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-aws/${HADOOP_VERSION}/hadoop-aws-${HADOOP_VERSION}.jar  

ENV HIVE_HOME /opt/hive
RUN HIVE_VERSION=3.1.3 && mkdir -p ${HIVE_HOME} \
  && curl -fsSL https://archive.apache.org/dist/hive/hive-${HIVE_VERSION}/apache-hive-${HIVE_VERSION}-bin.tar.gz | tar xz --no-same-owner --strip-components=1 -C $HIVE_HOME

ENV SPARK_IMAGE_TAG=${SPARK_VERSION}

COPY ./entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

RUN groupadd -r spark --gid=1000 && \
    useradd -r -g spark --uid=1000 -d ${SPARK_HOME} spark && \
    chown spark:spark -R ${SPARK_HOME}

WORKDIR $SPARK_HOME
USER spark

ENTRYPOINT ["/tini", "--", "/entrypoint.sh"]
CMD ["driver"]