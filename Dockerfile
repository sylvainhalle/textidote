FROM openjdk:8 AS build

ADD https://downloads.apache.org//ant/binaries/apache-ant-1.10.9-bin.tar.gz /root/ant.tar.gz
COPY . /build
WORKDIR /build

RUN tar -xzvf /root/ant.tar.gz && \
    mv apache-ant* /ant && \
    /ant/bin/ant download-deps && \
    /ant/bin/ant


FROM openjdk:8
COPY --from=build /build/textidote.jar /
ENTRYPOINT ["java", "-jar", "/textidote.jar"]

