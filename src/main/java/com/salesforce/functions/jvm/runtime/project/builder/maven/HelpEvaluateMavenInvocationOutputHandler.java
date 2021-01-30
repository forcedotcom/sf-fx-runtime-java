package com.salesforce.functions.jvm.runtime.project.builder.maven;

import java.util.Optional;
import java.util.regex.Pattern;

final class HelpEvaluateMavenInvocationOutputHandler implements MavenInvocationOutputHandler<Optional<String>> {
    private String evaluationResult;

    @Override
    public void consumeLine(String line) {
        if (!PATTERN.matcher(line).matches()) {
            evaluationResult = line;
        }
    }

    @Override
    public Optional<String> getResult() {
        return Optional.ofNullable(evaluationResult);
    }

    private final static Pattern PATTERN = Pattern.compile("^\\[[A-Z]+\\] .*$");
}
