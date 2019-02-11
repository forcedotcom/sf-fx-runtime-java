# Salesforce Function Runtime

Runtime for Salesforce Functions.

Requires "Salesforce Function SDK for Java" [repo](https://github.com/cwallsfdc/sf-fx-sdk-java).

## Build
```sh
$ mvn package
```

## Run

#### With a function in runtime jar:

```sh
$ java -jar target/com.salesforce.function.runtime-1.0-SNAPSHOT.jar
```

Check availability:
```
$ curl localhost:8080
Function 'TestFunction' is ready for service!
```

Invoke function:
```
$ curl -X POST localhost:8080/invoke
Test function invoked!
```

#### With an external jar containing function:

```sh
$ java -cp target/com.salesforce.function.runtime-1.0-SNAPSHOT.jar -Dloader.path=<PATH TO EXTERNAL JAR> org.springframework.boot.loader.PropertiesLauncher
```

Invoke function w/ POST content:
```
$ $ cat request.json
{"context":{"userContext":{"orgId":"00D","username":"admin@salesforce.com","userId":"005"}},"parameters":[]}

$ cat test-post.sh
curl -X POST --header "Content-Type: application/json" localhost:8080/invoke -d @request.json

$ ./test-post.sh
{"statusCode":200,"result":"Hello admin@salesforce.com!"}
```

## Test
```
$ mvn test
...
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 6.802 s
[INFO] Finished at: 2018-09-10T17:43:18-05:00
[INFO] ------------------------------------------------------------------------
```

