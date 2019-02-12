package com.unite;

import static org.junit.Assert.assertNotNull;

import com.salesforce.function.Function;
import com.salesforce.function.FunctionException;
import com.salesforce.function.request.Request;

public class TestFunction implements Function {

    @Override
	public String invoke(Request request) throws FunctionException {
        assertNotNull(request);
        assertNotNull(request.getContext());
        assertNotNull(request.getContext().getUserContext());
        return "Function invoked!";
	}

}