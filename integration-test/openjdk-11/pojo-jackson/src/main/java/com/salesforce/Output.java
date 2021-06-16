package com.salesforce;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Output {
    private final String result;

    public Output(String result) {
        this.result = result;
    }

    @JsonProperty("result")
    public String getTheResult() {
        return result;
    }
}
