/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.invocation.undertow;

import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.google.common.net.MediaType;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.salesforce.functions.jvm.runtime.InvocationInterface;
import com.salesforce.functions.jvm.runtime.project.ProjectFunction;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunctionResult;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.*;
import io.cloudevents.CloudEvent;
import io.cloudevents.http.HttpMessageFactory;
import io.cloudevents.rw.CloudEventRWException;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.*;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UndertowInvocationInterface
    implements InvocationInterface<
        CloudEvent, SalesforceFunctionResult, SalesforceFunctionException> {
  private final int port;
  private final String host;
  private Undertow undertow = null;

  private static final Logger LOGGER = LoggerFactory.getLogger(UndertowInvocationInterface.class);

  public UndertowInvocationInterface(int port, String host) {
    this.port = port;
    this.host = host;
  }

  @Override
  public void start(
      ProjectFunction<CloudEvent, SalesforceFunctionResult, SalesforceFunctionException>
          projectFunction) {
    this.undertow =
        Undertow.builder()
            .addHttpListener(port, host)
            .setHandler(new ProjectFunctionHandler(projectFunction))
            .build();

    undertow.start();
  }

  @Override
  public void stop() throws Exception {
    if (undertow != null) {
      undertow.stop();
      undertow = null;
    }
  }

  @Override
  public boolean isStarted() {
    return this.undertow != null;
  }

  @Override
  public void block() throws Exception {
    undertow.getWorker().awaitTermination();
  }

  private static class ProjectFunctionHandler implements HttpHandler {
    private final Gson gson = new Gson();

    private final ProjectFunction<CloudEvent, SalesforceFunctionResult, SalesforceFunctionException>
        projectFunction;

    public ProjectFunctionHandler(
        ProjectFunction<CloudEvent, SalesforceFunctionResult, SalesforceFunctionException>
            projectFunction) {
      this.projectFunction = projectFunction;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
      if (exchange.isInIoThread()) {
        exchange.dispatch(this);
        return;
      }

      exchange.startBlocking();

      // Step 1: Validate basic HTTP request data
      if (!exchange.getRequestMethod().equals(Methods.POST)) {
        makeResponse(
            exchange,
            StatusCodes.METHOD_NOT_ALLOWED,
            new JsonPrimitive("HTTP 405: Method Not Allowed"),
            new ExtraInfo());
        return;
      }

      if (!exchange.getRequestPath().equals("/")) {
        makeResponse(
            exchange,
            StatusCodes.NOT_FOUND,
            new JsonPrimitive("HTTP 404: Not Found"),
            new ExtraInfo());
        return;
      }

      // If the request is a health check request, stop processing and return a successful result as
      // per spec.
      HeaderValues healthCheckHeaders = exchange.getRequestHeaders().get("x-health-check");
      if (healthCheckHeaders != null && healthCheckHeaders.contains("true")) {
        makeResponse(exchange, StatusCodes.OK, new JsonPrimitive("OK"), new ExtraInfo());
        return;
      }

      // Step 2: Parse HTTP request as a CloudEvent
      final byte[] body = ByteStreams.toByteArray(exchange.getInputStream());

      Map<String, List<String>> headers = new HashMap<>();
      exchange
          .getRequestHeaders()
          .forEach(
              headerValues -> {
                String name = headerValues.getHeaderName().toString();
                List<String> values = new ArrayList<>(headerValues);
                headers.put(name, values);
              });

      final CloudEvent cloudEvent;
      try {
        cloudEvent = HttpMessageFactory.createReaderFromMultimap(headers, body).toEvent();
      } catch (IllegalStateException | CloudEventRWException e) {
        makeResponse(
            exchange,
            StatusCodes.BAD_REQUEST,
            new JsonPrimitive("Could not parse CloudEvent: " + e.getMessage()),
            new ExtraInfo().withInternalExceptionData(e));
        return;
      }

      // Step 3: Apply function with the CloudEvent, translating exceptions to semantic HTTP error
      // responses.
      try {
        long startNanoTime = System.nanoTime();
        SalesforceFunctionResult result = projectFunction.apply(cloudEvent);
        long elapsedNanoTime = System.nanoTime() - startNanoTime;

        // Currently, the HTTP interface only supports JSON results. Since the runtime supports
        // other content types as well, we need to make sure to only return JSON.
        if (!result.getMediaType().equals(MediaType.JSON_UTF_8)) {
          makeResponse(
              exchange,
              StatusCodes.SERVICE_UNAVAILABLE,
              new JsonPrimitive("Function returned non-JSON data which is unsupported!"),
              new ExtraInfo()
                  .withCloudEventData(cloudEvent)
                  .withFunctionExecutionTime(Duration.ofNanos(elapsedNanoTime)));
          return;
        }

        makeResponse(
            exchange,
            StatusCodes.OK,
            // We validated earlier that the data is indeed an UTF-8 encoded JSON string
            gson.fromJson(new String(result.getData(), StandardCharsets.UTF_8), JsonElement.class),
            new ExtraInfo()
                .withCloudEventData(cloudEvent)
                .withFunctionExecutionTime(Duration.ofNanos(elapsedNanoTime)));

      } catch (MalformedOrMissingSalesforceContextExtensionException e) {
        makeResponse(
            exchange,
            StatusCodes.BAD_REQUEST,
            new JsonPrimitive("CloudEvent is missing required sfcontext extension!"),
            new ExtraInfo().withCloudEventData(cloudEvent).withInternalExceptionData(e));

      } catch (MalformedOrMissingSalesforceFunctionContextExtensionException e) {
        makeResponse(
            exchange,
            StatusCodes.BAD_REQUEST,
            new JsonPrimitive("CloudEvent is missing required sffncontext extension!"),
            new ExtraInfo().withCloudEventData(cloudEvent).withInternalExceptionData(e));

      } catch (PayloadUnmarshallingException e) {
        makeResponse(
            exchange,
            StatusCodes.BAD_REQUEST,
            new JsonPrimitive("Could not unmarshall payload: " + e.getCause().getMessage()),
            new ExtraInfo().withCloudEventData(cloudEvent).withFunctionExceptionData(e));

      } catch (FunctionResultMarshallingException e) {
        makeResponse(
            exchange,
            StatusCodes.BAD_REQUEST,
            new JsonPrimitive("Could not marshall function result: " + e.getCause().getMessage()),
            new ExtraInfo().withCloudEventData(cloudEvent).withFunctionExceptionData(e));

      } catch (FunctionThrewExceptionException e) {
        String message =
            "Function threw exception: "
                + e.getCause().getClass().getName()
                + " ("
                + e.getCause().getMessage()
                + ")\n"
                // When running on Windows, the stack trace string will contain Windows
                // line-endings so we have to normalize them here:
                + Throwables.getStackTraceAsString(e.getCause()).replaceAll("\\r\\n", "\\\n");

        makeResponse(
            exchange,
            StatusCodes.INTERNAL_SERVER_ERROR,
            new JsonPrimitive(message),
            new ExtraInfo().withCloudEventData(cloudEvent).withFunctionExceptionData(e));

      } catch (SdkInitializationException e) {
        makeResponse(
            exchange,
            StatusCodes.SERVICE_UNAVAILABLE,
            new JsonPrimitive("Could not initialize SDK for function!"),
            new ExtraInfo().withCloudEventData(cloudEvent).withInternalExceptionData(e));

      } catch (SalesforceFunctionException e) {
        makeResponse(
            exchange,
            StatusCodes.SERVICE_UNAVAILABLE,
            new JsonPrimitive(
                "Unknown error while executing function: " + e.getCause().getMessage()),
            new ExtraInfo().withCloudEventData(cloudEvent).withInternalExceptionData(e));
      }
    }

    private void makeResponse(
        HttpServerExchange exchange, int status, JsonElement data, ExtraInfo extraInfo) {
      exchange.setStatusCode(status);
      exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "application/json");

      try {
        exchange
            .getResponseHeaders()
            .add(
                HttpString.tryFromString("x-extra-info"),
                URLEncoder.encode(gson.toJson(extraInfo.withStatusCode(status)), "UTF-8"));

      } catch (UnsupportedEncodingException | JsonSyntaxException e) {
        LOGGER.warn("Could not write x-extra-info header!", e);
      }

      exchange.getResponseSender().send(gson.toJson(data), StandardCharsets.UTF_8);
      exchange.getResponseSender().close();
    }
  }
}
