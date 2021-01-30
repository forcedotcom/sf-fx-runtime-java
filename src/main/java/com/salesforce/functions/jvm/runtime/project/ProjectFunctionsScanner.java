package com.salesforce.functions.jvm.runtime.project;

import java.util.List;

public interface ProjectFunctionsScanner<F extends ProjectFunction<T, R, E>, T, R, E extends Throwable> {
    List<F> scan(Project project);
}
