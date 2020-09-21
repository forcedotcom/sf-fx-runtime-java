package com.salesforce.functions.jvm.runtime;

import com.salesforce.functions.jvm.runtime.project.ProjectFunction;

/**
 * An InvocationInterface provides an interface for the user to invoke their function. This could be an embedded
 * web server that translates requests into function calls, a handler for a binary protocol, a reader for a queue of
 * function calls or anything else.
 */
public interface InvocationInterface {
    /**
     * Returns if this InvocationInterface can handle the given function.
     *
     * @param projectFunction The function to check.
     * @return If the function can be handled.
     */
    boolean canHandle(ProjectFunction projectFunction);

    /**
     * Starts the InvocationInterface for the given function. Implementations are supposed to block until they
     * terminate.
     *
     * @param projectFunction The function to expose via this InvocationInterface.
     * @throws Exception When the implementation cannot recover from an error and needs to terminate.
     */
    void start(ProjectFunction projectFunction) throws Exception;
}
