package com.salesforce.functions.jvm.runtime.project.builder.maven;

public interface MavenInvocationOutputHandler<A> extends org.apache.maven.shared.invoker.InvocationOutputHandler {
    A getResult();
}
