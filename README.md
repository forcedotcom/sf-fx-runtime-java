# sf-fx-runtime-java
[![](https://badgen.net/github/license/forcedotcom/sf-fx-runtime-java)](LICENSE.txt)
[![](https://badgen.net/maven/v/maven-central/com.salesforce.functions/sf-fx-runtime-java)](https://search.maven.org/artifact/com.salesforce.functions/sf-fx-runtime-java)
[![](https://badgen.net/circleci/github/forcedotcom/sf-fx-runtime-java/main)](https://circleci.com/gh/forcedotcom/sf-fx-runtime-java/tree/main)
[![](https://codecov.io/gh/forcedotcom/sf-fx-runtime-java/branch/main/graph/badge.svg)](https://codecov.io/gh/forcedotcom/sf-fx-runtime-java)
[![](https://badgen.net/github/dependabot/forcedotcom/sf-fx-runtime-java)](https://github.com/forcedotcom/sf-fx-runtime-java/security/dependabot)

Java **language runtime** for Salesforce Functions. For the Java functions **SDK** see [forcedotcom/sf-fx-sdk-java](https://github.com/forcedotcom/sf-fx-sdk-java) 
and for the **Cloud Native Buildpacks** that assemble the Java function containers, see [heroku/buildpacks-jvm](https://github.com/heroku/buildpacks-jvm).

## Early Release Note
This feature is in beta and has been released early so we can collect feedback. It may contain significant problems, undergo major changes, or be discontinued. The use of this feature is governed by the [Salesforce.com Program Agreement](https://trailblazer.me/terms?lan=en).

## Local Development
### Building

This project uses [Maven Wrapper](https://github.com/takari/maven-wrapper), run `./mvnw package` to build and package 
the runtime. It requires at least OpenJDK 8.

### Running
This project includes the shell script `sf-fx-runtime-java` to run the project locally after building.

```
Usage: sf-fx-runtime-java [COMMAND]
Salesforce Functions Java Runtime
Commands:
  serve   Serves a function project via HTTP
  bundle  Pre-bundles a function project
  help    Displays help information about the specified command

See 'sf-fx-runtime-java help <command>' to read about a specific subcommand.
```

#### Serving a Function
```
$ ./sf-fx-runtime-java serve ~/projects/my-function
```

```
Serves a function project via HTTP
Usage: sf-fx-runtime-java serve [-h=<host>] [-p=<port>] <projectPath>
      <projectPath>   The directory that contains the function(s)
  -h, --host=<host>   The host the webserver should bind to. Defaults to
                        'localhost'.
  -p, --port=<port>   The port the webserver should listen on. Defaults to
                        '8080'.
```

#### Bundling a Function Project
```
$ ./sf-fx-runtime-java bundle ~/projects/my-function ~/functions/my-function
```

```
Pre-bundles a function project
Usage: sf-fx-runtime-java bundle <projectPath> <bundlePath>
      <projectPath>   The directory that contains the function(s)
      <bundlePath>    The directory to write the bundle to
```

### Generating Code Coverage Reports
```
$ ./mvnw clean package -Paggregate-coverage
```

After building, an aggregated report across all project modules can be found at [coverage/target/site/jacoco-aggregate/index.html](coverage/target/site/jacoco-aggregate/index.html)
