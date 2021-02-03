# sf-fx-runtime-java

Note: This feature is in beta and has been released early so we can collect feedback. It may contain significant problems, undergo major changes, or be discontinued. The use of this feature is governed by the [Salesforce.com Program Agreement](https://trailblazer.me/terms?lan=en).

## Building

This project uses [Maven Wrapper](https://github.com/takari/maven-wrapper), run `./mvnw package` to build and package 
the runtime. It requires at least OpenJDK 8.

## Running

This project includes a shell script to run the project locally after building. To serve a function via HTTP:

```
$ ./sf-fx-runtime-java serve ~/projects/my-function
```

```
Usage: sf-fx-runtime-java [COMMAND]
Salesforce Functions Java Runtime
Commands:
serve   Serves a function project via HTTP
bundle  Pre-bundles a function project
help    Displays help information about the specified command

See 'sf-fx-runtime-java help <command>' to read about a specific subcommand.
```

```
Pre-bundles a function project
Usage: sf-fx-runtime-java bundle <projectPath> <bundlePath>
      <projectPath>   The directory that contains the function(s)
      <bundlePath>    The directory to write the bundle to
```

```
Serves a function project via HTTP
Usage: sf-fx-runtime-java serve [-p=<port>] <projectPath>
      <projectPath>   The directory that contains the function(s)
  -p, --port=<port>   The port the webserver should listen on.
```
