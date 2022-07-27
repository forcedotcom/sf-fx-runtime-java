package com.salesforce;

import com.salesforce.functions.jvm.sdk.Context;
import com.salesforce.functions.jvm.sdk.InvocationEvent;
import com.salesforce.functions.jvm.sdk.SalesforceFunction;
import com.salesforce.functions.jvm.sdk.data.DataApi;

public class StringReverseFunction implements SalesforceFunction<String, String> {
    @Override
    public String apply(InvocationEvent<String> event, Context context) throws Exception {
        return new StringBuilder(event.getData()).reverse().toString();
    }
}
