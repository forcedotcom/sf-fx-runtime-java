package com.salesforce.functions.jvm.runtime.invocation.jetty;

import com.google.common.io.ByteStreams;
import com.salesforce.functions.jvm.runtime.project.ProjectFunction;
import com.salesforce.functions.jvm.runtime.InvocationInterface;
import com.salesforce.functions.jvm.runtime.cloudevent.SalesforceCloudEventFunction;
import com.salesforce.functions.jvm.runtime.cloudevent.exception.*;
import io.cloudevents.CloudEvent;
import io.cloudevents.http.HttpMessageFactory;
import io.cloudevents.rw.CloudEventRWException;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class JettyCloudEventInvocationInterface implements InvocationInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(JettyCloudEventInvocationInterface.class);

    @Override
    public boolean canHandle(ProjectFunction projectFunction) {
        try {
            new SalesforceCloudEventFunction(projectFunction);
            return true;
        } catch (IncompatibleFunctionException e) {
            return false;
        }
    }

    @Override
    public void start(ProjectFunction projectFunction) throws Exception {
        SalesforceCloudEventFunction salesforceCloudEventFunction = new SalesforceCloudEventFunction(projectFunction);

        String portString = System.getProperty("invocation-interface.jetty.port", "8080");
        if (!portString.matches("^\\d+$")) {
            throw new IllegalStateException("System property 'invocation-interface.jetty.port' must be an integer!");
        }

        Server server = new Server(Integer.parseInt(portString));
        server.setHandler(new HttpCloudEventInvocationInterfaceHandler(salesforceCloudEventFunction));

        server.start();
        server.join();
    }

    private static class HttpCloudEventInvocationInterfaceHandler extends AbstractHandler {
        private SalesforceCloudEventFunction salesforceCloudEventFunction;
        private static final Logger LOGGER = LoggerFactory.getLogger(HttpCloudEventInvocationInterfaceHandler.class);

        public HttpCloudEventInvocationInterfaceHandler(SalesforceCloudEventFunction salesforceCloudEventFunction) {
            this.salesforceCloudEventFunction = salesforceCloudEventFunction;
        }

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
            baseRequest.setHandled(true);

            // Step 1: Validate basic HTTP request data
            if (!request.getMethod().equals("POST")) {
                makePlainTextErrorResponse(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "");
                return;
            }

            if (!request.getRequestURI().equals("/")) {
                makePlainTextErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Not Found");
                return;
            }

            // Step 2: Parse HTTP request as a CloudEvent
            final byte[] body = ByteStreams.toByteArray(request.getInputStream());

            final Map<String, String> headers = new HashMap<>();
            final Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headers.put(headerName, request.getHeader(headerName));
            }

            final CloudEvent cloudEvent;
            try {
                cloudEvent = HttpMessageFactory.createReader(headers, body).toEvent();
            } catch (IllegalStateException | CloudEventRWException e) {
                final String message = "Could not parse CloudEvent: " + e.getMessage();
                makePlainTextErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, message);
                return;
            }

            // Step 3: Apply function with the CloudEvent, translating exceptions to semantic HTTP error responses.
            try {
                String resultJsonString = salesforceCloudEventFunction.apply(cloudEvent);

                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.getWriter().println(resultJsonString);

            } catch (MissingCloudEventDataException e) {
                String message = "CloudEvent does not contain any data to pass to the function!";
                makePlainTextErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, message);
            } catch (IncompatibleCloudEventDataContentTypeException e) {
                String message = "CloudEvent data must be of type application/json!";
                makePlainTextErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, message);
            } catch (IncompatibleCloudEventTypeException e) {
                String message = "CloudEvent must be of type 'com.salesforce.function.invoke.sync'!";
                makePlainTextErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, message);
            } catch (MalformedOrMissingSalesforceContextExtensionException e) {
                String message = "CloudEvent is missing required 'sfcontext' extension!";
                makePlainTextErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, message);
            } catch (MalformedOrMissingSalesforceFunctionContextExtensionException e) {
                String message = "CloudEvent is missing required 'sffncontext' extension!";
                makePlainTextErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, message);
            } catch (FunctionParameterJsonDeserializationException e) {
                String message = "Could not deserialize JSON for function parameter!";
                if (e.getCause() != null) {
                    message += "(" + e.getCause().getMessage() + ")";
                }

                makePlainTextErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, message);
            } catch (SdkInitializationException e) {
                String message = "Could not initialize SDK for function!";
                makePlainTextErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
            } catch (FunctionThrewExceptionException e) {
                String message = "Function threw exception: " + e.getCause().getClass().getName() + " (" + e.getCause().getMessage() + ")";
                makePlainTextErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
            } catch (SalesforceCloudEventFunctionException e) {
                String message = "Unknown error while executing function: " + e.getMessage();
                makePlainTextErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
            }
        }

        private void makePlainTextErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
            response.setStatus(status);
            response.setContentType("text/plain; charset=utf-8");
            response.getWriter().println(message);
        }
    }
}
