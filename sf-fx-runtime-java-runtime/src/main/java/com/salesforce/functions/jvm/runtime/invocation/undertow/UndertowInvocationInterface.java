/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.invocation.undertow;

import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
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
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
  private static final Logger LOGGER = LoggerFactory.getLogger(UndertowInvocationInterface.class);

  public UndertowInvocationInterface(int port) {
    this.port = port;
  }

  @Override
  public void start(
      ProjectFunction<CloudEvent, SalesforceFunctionResult, SalesforceFunctionException>
          projectFunction)
      throws Exception {
    Undertow undertow =
        Undertow.builder()
            .addHttpListener(port, "")
            .setHandler(new ProjectFunctionHandler(projectFunction))
            .build();

    undertow.start();
    undertow.getWorker().awaitTermination();
  }

  private static class ProjectFunctionHandler implements HttpHandler {
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
        makePlainTextErrorResponse(exchange, StatusCodes.METHOD_NOT_ALLOWED, "");
        return;
      }

      if (!exchange.getRequestPath().equals("/")) {
        makePlainTextErrorResponse(exchange, StatusCodes.NOT_FOUND, "Not Found");
        return;
      }

      // If the request is a health check request, stop processing and return a successful result as
      // per spec.
      HeaderValues healthCheckHeaders = exchange.getRequestHeaders().get("x-health-check");
      if (healthCheckHeaders != null && healthCheckHeaders.contains("true")) {
        exchange.setStatusCode(StatusCodes.OK);
        exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send("\"OK\"", StandardCharsets.UTF_8);
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
        final String message = "Could not parse CloudEvent: " + e.getMessage();
        makePlainTextErrorResponse(exchange, StatusCodes.BAD_REQUEST, message);
        return;
      }

      // Step 3: Apply function with the CloudEvent, translating exceptions to semantic HTTP error
      // responses.
      try {
        SalesforceFunctionResult result = projectFunction.apply(cloudEvent);

        exchange.setStatusCode(StatusCodes.OK);
        exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, result.getMediaType().toString());
        exchange.getOutputStream().write(result.getData());
      } catch (MissingCloudEventDataException e) {
        String message = "CloudEvent does not contain any data to pass to the function!";
        makePlainTextErrorResponse(exchange, StatusCodes.BAD_REQUEST, message);
      } catch (IncompatibleCloudEventDataContentTypeException e) {
        String message = "CloudEvent data must be of type application/json!";
        makePlainTextErrorResponse(exchange, StatusCodes.BAD_REQUEST, message);
      } catch (IncompatibleCloudEventTypeException e) {
        String message = "CloudEvent must be of type 'com.salesforce.function.invoke.sync'!";
        makePlainTextErrorResponse(exchange, StatusCodes.BAD_REQUEST, message);
      } catch (MalformedOrMissingSalesforceContextExtensionException e) {
        String message = "CloudEvent is missing required 'sfcontext' extension!";
        makePlainTextErrorResponse(exchange, StatusCodes.BAD_REQUEST, message);
      } catch (MalformedOrMissingSalesforceFunctionContextExtensionException e) {
        String message = "CloudEvent is missing required 'sffncontext' extension!";
        makePlainTextErrorResponse(exchange, StatusCodes.BAD_REQUEST, message);
      } catch (SdkInitializationException e) {
        String message = "Could not initialize SDK for function!";
        makePlainTextErrorResponse(exchange, StatusCodes.INTERNAL_SERVER_ERROR, message);
      } catch (FunctionThrewExceptionException e) {
        String message =
            "Function threw exception: "
                + e.getCause().getClass().getName()
                + " ("
                + e.getCause().getMessage()
                + ")\n"
                + Throwables.getStackTraceAsString(e.getCause());

        makePlainTextErrorResponse(exchange, StatusCodes.INTERNAL_SERVER_ERROR, message);
      } catch (PayloadUnmarshallingException e) {
        LOGGER.warn("Could not unmarshall function payload!", e);
        String message = "Could not unmarshall payload: " + e.getCause().getMessage();
        makePlainTextErrorResponse(exchange, StatusCodes.BAD_REQUEST, message);
      } catch (FunctionResultMarshallingException e) {
        LOGGER.warn("Could not marshall function result!", e);
        String message = "Could not marshall function result: " + e.getCause().getMessage();
        makePlainTextErrorResponse(exchange, StatusCodes.BAD_REQUEST, message);
      } catch (SalesforceFunctionException e) {
        String message = "Unknown error while executing function: " + e.getMessage();
        makePlainTextErrorResponse(exchange, StatusCodes.INTERNAL_SERVER_ERROR, message);
      }
    }

    private void makePlainTextErrorResponse(HttpServerExchange exchange, int status, String message)
        throws IOException {
      exchange.setStatusCode(status);
      exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "text/plain; charset=utf-8");
      exchange.getResponseSender().send(message + "\n", StandardCharsets.UTF_8);
    }
  }
}
