FROM java:8
WORKDIR /
ADD target/ org.ConsumerApplication-1.0-SNAPSHOT.jar consumer.jar
CMD java -jar consumer.jar
