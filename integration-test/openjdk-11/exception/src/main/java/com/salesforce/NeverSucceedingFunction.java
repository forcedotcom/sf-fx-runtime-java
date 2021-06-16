package com.salesforce;

import com.salesforce.functions.jvm.sdk.Context;
import com.salesforce.functions.jvm.sdk.InvocationEvent;
import com.salesforce.functions.jvm.sdk.SalesforceFunction;

public class NeverSucceedingFunction implements SalesforceFunction<String, String> {
    @Override
    public String apply(InvocationEvent<String> event, Context context) throws Exception {
        throw new IllegalStateException("This function never succeeds!");
    }
}
