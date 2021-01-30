package com.salesforce.functions.jvm.runtime.sfjavafunction.api;

import java.lang.reflect.Method;
import java.util.Arrays;

public final class ApiUtil {
    public static UnsupportedOperationException createUnsupportedOperationException(Method method) {
        String methodIdentifier = String.format(
                "%s %s (%s)",
                method.getReturnType(),
                method.getName(),
                String.join(", ", Arrays.deepToString(method.getParameterTypes()))
        );

        return new UnsupportedOperationException(methodIdentifier);
    }
}
