package com.salesforce;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Input {
    private String name;
    private int age;

    public Input(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @JsonProperty("name")
    public String getTheName() {
        return name;
    }

    @JsonProperty("age")
    public int getTheAge() {
        return age;
    }
}
