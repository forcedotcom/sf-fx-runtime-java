# sf-fx-runtime-java

Note: This feature is in beta and has been released early so we can collect feedback. It may contain significant problems, undergo major changes, or be discontinued. The use of this feature is governed by the [Salesforce.com Program Agreement](https://trailblazer.me/terms?lan=en).

## Building

This project uses [Maven Wrapper](https://github.com/takari/maven-wrapper), run `./mvnw package` to build and package 
the runtime. It requires at least OpenJDK 8.

## Running

This project includes a shell script to run the project locally after building. It accepts a function project directory 
as the first parameter:

```
$ ./run.sh ~/projects/my-function
```
