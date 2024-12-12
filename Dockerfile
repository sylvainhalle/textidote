FROM openjdk:8 AS build

# ADD https://archive.apache.org/dist/ant/binaries/apache-ant-1.10.11-bin.tar.gz /root/ant.tar.gz
ADD https://downloads.apache.org/ant/binaries/apache-ant-1.10.15-bin.tar.gz /root/ant.tar.gz
COPY . /build
WORKDIR /build

#RUN tar -xzvf /root/ant.tar.gz && \
#    mv apache-ant* /ant && \
#    /ant/bin/ant download-deps && \
#    /ant/bin/ant


RUN tar -xzvf /root/ant.tar.gz
RUN mv apache-ant* /ant
RUN /ant/bin/ant wipe
RUN /ant/bin/ant download-deps
RUN /ant/bin/ant

FROM openjdk:8
COPY --from=build /build/textidote-0.9.jar /textidote.jar
ENTRYPOINT ["java", "-jar", "/textidote.jar"]

