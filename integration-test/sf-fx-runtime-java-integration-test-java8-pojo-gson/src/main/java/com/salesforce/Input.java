package com.salesforce;

import com.google.gson.annotations.SerializedName;

public class Input {
    @SerializedName("name")
    private final String weirdInternalNameForName;
    @SerializedName("age")
    private final int __age;

    public Input(String name, int age) {
        this.weirdInternalNameForName = name;
        this.__age = age;
    }

    public String getName() {
        return weirdInternalNameForName;
    }

    public int getAge() {
        return __age;
    }
}
