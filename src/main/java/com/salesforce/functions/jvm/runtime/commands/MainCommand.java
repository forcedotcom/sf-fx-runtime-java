package com.salesforce.functions.jvm.runtime.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.util.concurrent.Callable;

@Command(name = "sf-fx-runtime-java",
        description = "Salesforce Functions Java Runtime",
        footer = "%nSee 'sf-fx-runtime-java help <command>' to read about a specific subcommand.",
        subcommands = {
                ServeCommand.class,
                BundleCommand.class,
                CommandLine.HelpCommand.class
        }
)
public class MainCommand implements Callable<Integer> {
    @Spec
    CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        spec.commandLine().usage(System.err);
        return 1;
    }
}
