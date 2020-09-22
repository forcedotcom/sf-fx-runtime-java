package com.salesforce.functions.jvm.runtime.project.maven;

public interface MavenInvocationOutputHandler<A> extends org.apache.maven.shared.invoker.InvocationOutputHandler {
    A getResult();
}
