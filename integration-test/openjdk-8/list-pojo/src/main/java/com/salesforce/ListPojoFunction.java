package com.salesforce;

import com.salesforce.functions.jvm.sdk.Context;
import com.salesforce.functions.jvm.sdk.InvocationEvent;
import com.salesforce.functions.jvm.sdk.SalesforceFunction;

import java.util.List;
import java.util.stream.Collectors;

public class ListPojoFunction implements SalesforceFunction<List<Input>, Output> {
    @Override
    public Output apply(InvocationEvent<List<Input>> event, Context context) throws Exception {
        List<Input> listData = event.getData();

        return new Output(
            listData.stream().map(data -> String.format("Hello %s, you are %d years old!", data.getName(), data.getAge())).collect(Collectors.joining(" - "))
        );
    }
}
