package com.salesforce;

import com.salesforce.functions.jvm.sdk.Context;
import com.salesforce.functions.jvm.sdk.InvocationEvent;
import com.salesforce.functions.jvm.sdk.SalesforceFunction;

public class PojoFunction implements SalesforceFunction<Input, Output> {
    @Override
    public Output apply(InvocationEvent<Input> event, Context context) throws Exception {
        Input data = event.getData();

        return new Output(
            String.format("Hello %s, you are %d years old!", data.getName(), data.getAge())
        );
    }
}
