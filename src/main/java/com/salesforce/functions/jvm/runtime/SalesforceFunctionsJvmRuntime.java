package com.salesforce.functions.jvm.runtime;

import com.salesforce.functions.jvm.runtime.commands.MainCommand;
import picocli.CommandLine;

public final class SalesforceFunctionsJvmRuntime {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new MainCommand()).execute(args);
        System.exit(exitCode);
    }
}
