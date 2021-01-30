package com.salesforce.functions.jvm.runtime.invocation.jetty;

import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.salesforce.functions.jvm.runtime.sfjavafunction.SalesforceFunctionResult;
import com.salesforce.functions.jvm.runtime.project.ProjectFunction;
import com.salesforce.functions.jvm.runtime.InvocationInterface;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.*;
import io.cloudevents.CloudEvent;
import io.cloudevents.http.*;
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

public class JettyCloudEventInvocationInterface implements InvocationInterface<CloudEvent, SalesforceFunctionResult, SalesforceFunctionException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JettyCloudEventInvocationInterface.class);
    private final int port;

    public JettyCloudEventInvocationInterface(int port) {
        this.port = port;
    }

    @Override
    public void start(ProjectFunction<CloudEvent, SalesforceFunctionResult, SalesforceFunctionException> projectFunction) throws Exception {
        Server server = new Server(port);
        server.setHandler(new HttpCloudEventInvocationInterfaceHandler(projectFunction));

        server.start();
        server.join();
    }

    private static class HttpCloudEventInvocationInterfaceHandler extends AbstractHandler {
        private final ProjectFunction<CloudEvent, SalesforceFunctionResult, SalesforceFunctionException> projectFunction;
        private static final Logger LOGGER = LoggerFactory.getLogger(HttpCloudEventInvocationInterfaceHandler.class);

        public HttpCloudEventInvocationInterfaceHandler(ProjectFunction<CloudEvent, SalesforceFunctionResult, SalesforceFunctionException> salesforceCloudEventFunction) {
            this.projectFunction = salesforceCloudEventFunction;
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
                SalesforceFunctionResult result = projectFunction.apply(cloudEvent);

                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType(result.getMediaType().toString());
                response.getOutputStream().write(result.getData());

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
            } catch (SdkInitializationException e) {
                String message = "Could not initialize SDK for function!";
                makePlainTextErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
            } catch (FunctionThrewExceptionException e) {
                String message = "Function threw exception: " +
                        e.getCause().getClass().getName() +
                        " (" +
                        e.getCause().getMessage() +
                        ")\n" +
                        Throwables.getStackTraceAsString(e.getCause());

                makePlainTextErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
            } catch (PayloadUnmarshallingException e) {
                String message = "Could not unmarshall payload: " + e.getCause().getMessage();
                makePlainTextErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, message);
            } catch (SalesforceFunctionException e) {
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
