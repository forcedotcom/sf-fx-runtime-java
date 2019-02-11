package com.salesforce.function.runtime;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "No function found")
public class NoFunctionFound extends RuntimeException {

    private static final long serialVersionUID = 1L;

}