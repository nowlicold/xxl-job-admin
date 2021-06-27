FROM openjdk:8-jre-slim
MAINTAINER xuxueli

ENV PARAMS=""

ENV TZ=PRC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ADD target/xxl-job-admin-*.jar /app.jar

ENV JAVA_OPTS "-Xmx512M -Xms512M"
ENV SPRING_OPTS "--spring.profiles.active=local"
ENTRYPOINT ["sh","-c","java -Dfile.encoding=UTF-8 ${JAVA_OPTS} -jar /app.jar ${SPRING_OPTS} $PARAMS"]
