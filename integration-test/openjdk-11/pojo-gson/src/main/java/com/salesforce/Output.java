package com.salesforce;

import com.google.gson.annotations.SerializedName;

public class Output {
    @SerializedName("result")
    private final String thisIsNotCalledResultResultInternally;

    public Output(String result) {
        this.thisIsNotCalledResultResultInternally = result;
    }
}
