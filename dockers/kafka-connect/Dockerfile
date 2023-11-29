# From a  base image with Kafka Connect installedFROM quay.io/strimzi/kafka:latest-kafka-3.0.0
FROM quay.io/strimzi/kafka:latest-kafka-3.0.0

USER root:root
COPY ./plugins/ /opt/kafka/plugins/
USER 1001