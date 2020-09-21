# sf-fx-runtime-java

## Building

This project uses [Maven Wrapper](https://github.com/takari/maven-wrapper), run `./mvnw package` to build and package 
the runtime. It requires at least OpenJDK 8.

### Prerequisites
This application currently depends on a `SNAPSHOT` version of 
[cloudevents/sdk-java](https://github.com/cloudevents/sdk-java) for generic HTTP binding support. As soon as 
`2.0.0-milestone3` is released, installing the `SNAPSHOT` version before building is no longer necessary.

```
$ git clone git@github.com:cloudevents/sdk-java.git
$ cd sdk-java 
$ ./mvnw install
``` 

## Running

This project includes a shell script to run the project locally after building. It accepts a function project directory 
as the first parameter:

```
$ ./run.sh ~/projects/my-function
```
