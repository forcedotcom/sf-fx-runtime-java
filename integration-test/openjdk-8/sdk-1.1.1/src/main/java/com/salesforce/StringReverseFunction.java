package com.salesforce;

import com.salesforce.functions.jvm.sdk.Context;
import com.salesforce.functions.jvm.sdk.InvocationEvent;
import com.salesforce.functions.jvm.sdk.SalesforceFunction;
import com.salesforce.functions.jvm.sdk.data.DataApi;
import com.salesforce.functions.jvm.sdk.data.Record;

public class StringReverseFunction implements SalesforceFunction<String, String> {
    @Override
    public String apply(InvocationEvent<String> event, Context context) throws Exception {
        // the only new operator we added to the SDK in 1.1.1 was to `RecordAccessor.getRecordField(name)`
        // so we'll create a dummy record and see if that method resolves fine
        context.getOrg().get().getDataApi().newRecordBuilder("Account")
                .withField("Name", "TestName")
                .build()
                .getRecordField("Owner");

        return new StringBuilder(event.getData()).reverse().toString();
    }
}
