# Module Title

user management

## Getting Started

### Prerequisites

* [JDK 8](http://www.oracle.com/technetwork/pt/java/javase/overview/index.html)

Ensure JAVA_HOME environment variable is set and points to your JDK installation

* [Maven](https://maven.apache.org/) - Dependency Management
Download from https://maven.apache.org/

```
unzip apache-maven-3.6.0-bin.zip
```

or
```
tar xzvf apache-maven-3.6.0-bin.tar.gz
```
Add the bin directory of the created directory apache-maven-3.5.4 to the PATH environment variable
Confirm with mvn -v in a new shell.

### Installing & Running the tests

Run below command to install the dependencies, compile and run the tests
```
$ mvn clean install
```

### And coding style tests

Test cases coverage should be 90% above

```
@Test
public void method_Shouldxxxx_Ifxxxx() {

}
```

## Deployment

JAR package will be created under target/user-management-{versionNo}.jar after packaging
then you can run below command to bring up the application
```
java -jar target/user-management-{versionNo}.jar
```


## Built With

* [Maven](https://maven.apache.org/)

## DB

Using in-memory DB H2 for this project. All info will be gone once restart.

## Versioning

We use GIT for versioning.

## Usage

* Same as other spring boot application, to run this module can just run the class Application which is annotated with @SpringBootApplication

* The application is running on 8080 port by default, it can be switch to other port by setting it in application.yml

## System Test

* APIs are tested with [postman](https://www.getpostman.com/downloads/). 

* Please import user-management.postman_collection.json to your postman for the testing

## Authors

* **Lin Lin**