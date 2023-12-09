FROM adoptopenjdk/openjdk11:jdk-11.0.11_9
COPY target/scala-2.13/kafka-sms-analyser-0.1.0.jar .

RUN mkdir /opt/kafka-sms-analyser
CMD ["java", "-jar", "kafka-sms-analyser-0.1.0.jar"]


# COPY ./log4j2.xml .


