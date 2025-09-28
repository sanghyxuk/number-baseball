# 1단계: Maven을 사용하여 Java 애플리케이션 빌드
# Java 17 버전을 기반으로 하는 Maven 이미지를 사용합니다.
# 만약 다른 Java 버전을 사용하신다면 이 부분을 수정해주세요. (예: maven:3.8.5-openjdk-11)
FROM maven:3.8.5-openjdk-17 AS build

# 작업 디렉토리 설정
WORKDIR /app

# Maven pom.xml 파일을 복사하여 의존성을 먼저 다운로드합니다.
# 이를 통해 소스 코드가 변경되어도 매번 의존성을 새로 받지 않아 빌드 속도가 향상됩니다.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 소스 코드를 복사합니다.
COPY src ./src

# Maven을 사용하여 애플리케이션을 빌드하고 실행 가능한 jar 파일을 생성합니다.
# -DskipTests 옵션으로 테스트는 생략하여 빌드 시간을 단축합니다.
RUN mvn package -DskipTests

# 2단계: 실제 실행을 위한 경량 이미지 생성
# 더 작은 크기의 JRE(Java Runtime Environment) 이미지를 사용하여 최종 이미지 크기를 줄입니다.
FROM openjdk:17-jre-slim

# 작업 디렉토리 설정
WORKDIR /app

# 1단계(build 스테이지)에서 생성된 jar 파일을 복사합니다.
# target/*.jar 패턴을 사용하여 파일 이름이 변경되어도 동작하도록 합니다.
COPY --from=build /app/target/*.jar app.jar

# Render가 애플리케이션에 연결할 포트를 지정합니다.
EXPOSE 8080

# 컨테이너가 시작될 때 실행할 명령어를 정의합니다.
# java -jar app.jar 명령으로 Spring Boot 애플리케이션을 실행합니다.
ENTRYPOINT ["java", "-jar", "app.jar"]

