FROM maven:3.8.6-openjdk-11-slim

WORKDIR /workspace

ENV DEBIAN_FRONTEND=noninteractive
RUN rm -f /etc/apt/apt.conf.d/docker-clean && \
    echo 'Binary::apt::APT::Keep-Downloaded-Packages "true";' > /etc/apt/apt.conf.d/keep-cache && \
    echo 'Acquire::Check-Valid-Until "false";' > /etc/apt/apt.conf.d/10no-check-valid-until && \
    echo 'Acquire::Check-Date "false";' > /etc/apt/apt.conf.d/10no-check-date && \
    echo 'Acquire::Retries "3";' > /etc/apt/apt.conf.d/80-retries && \
    echo "deb http://mirrors.ustc.edu.cn/debian bullseye main\ndeb http://mirrors.ustc.edu.cn/debian-security bullseye-security main\ndeb http://mirrors.ustc.edu.cn/debian bullseye-updates main" > /etc/apt/sources.list && \
    apt-get update && \
    apt-get install -y dos2unix && \
    rm -rf /var/lib/apt/lists/*

COPY . /workspace

RUN mvn -N io.takari:maven:0.7.7:wrapper && \
    dos2unix /workspace/mvnw && \
    chmod +x /workspace/mvnw 


CMD /workspace/mvnw spring-boot:run