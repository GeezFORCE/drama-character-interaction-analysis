FROM maven:3.9.6-eclipse-temurin-23 as build

WORKDIR /helidon

# Create a first layer to cache the "Maven World" in the local repository.
# Incremental docker builds will always resume after that, unless you update
# the pom
ADD pom.xml .
ADD common/pom.xml common/
ADD scraper/pom.xml scraper/
ADD processor/pom.xml processor/
ADD visualizer/pom.xml visualizer/

# Do a full build of the dependencies
RUN mvn package -DskipTests

# Create the actual image
FROM eclipse-temurin:23-jre-alpine

WORKDIR /helidon

# Copy the binary built in the 1st stage
COPY --from=build /helidon/visualizer/target/visualizer.jar ./
COPY --from=build /helidon/visualizer/target/libs ./libs

CMD ["java", "-jar", "visualizer.jar"]

EXPOSE 8083