FROM ubuntu:latest

RUN apt-get update
RUN apt-get install -y openjdk-11-jre openjdk-11-jdk
RUN apt-get install -y maven

RUN rm -rf /var/lib/apt/lists/*

WORKDIR /app