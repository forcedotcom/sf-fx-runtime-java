package com.salesforce.function.runtime;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.salesforce.function.Function;
import com.salesforce.function.FunctionException;
import com.salesforce.function.context.Context;
import com.salesforce.function.context.UserContext;
import com.salesforce.function.log.Logger;
import com.salesforce.function.request.ErrorResponse;
import com.salesforce.function.request.Request;
import com.salesforce.function.request.Response;
import com.salesforce.function.request.impl.ErrorResponseImpl;
import com.salesforce.function.request.impl.ResponseImpl;

/**
 * FIXME
 */
@RestController
public class FunctionController {

	@Autowired
	private ApplicationContext appCtx;

	@Autowired
	Function function;

	@RequestMapping("/")
	public Object index() {
		return function == null ?
				"No function found!" :
					"Function '" + function.getName() + "' is ready for service!";
	}

	@RequestMapping(
			value = "/invoke",
			method = RequestMethod.POST,
			produces = "application/json")
	public Object invoke(@RequestHeader Map<String, String> headers,
			@RequestBody Request request) {

		Context ctx = request.getContext();
		UserContext userCtx = ctx.getUserContext();
		Logger logger = ctx.getLogger();
		logger.info(headers.toString());
		logger.info("Received request from user " + userCtx.getUserId() + " , org " + userCtx.getOrgId());
		logger.info("Parameters: " + request.getParameters());
		logger.info("Forwarding request to " + function.getClass().getSimpleName() + "...");

		try {
			Object result = function.invoke(request);
			return new ResponseEntity<Response>(new ResponseImpl(result), HttpStatus.OK);
		} catch (FunctionException ex) {
			// REVIEWME: Response interface?
			// REVIEWME: look into what /error does
			ErrorResponse error = new ErrorResponseImpl(ex.getMessage(), ex);
			return new ResponseEntity<ErrorResponse>(error, HttpStatus.BAD_REQUEST);
		}
	}
}
