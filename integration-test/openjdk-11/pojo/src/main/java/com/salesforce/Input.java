package com.salesforce;

public class Input {
    private final String name;
    private final int age;

    public Input(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}
