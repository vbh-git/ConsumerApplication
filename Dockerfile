
FROM java:8
WORKDIR /
ADD target/*.jar consumer.jar
CMD java -jar consumer.jar
