package io.kubeless;

import io.kubeless.Event;
import io.kubeless.Context;

public class Demo {
    public String hello(io.kubeless.Event event, io.kubeless.Context context) {
        return "Hello java world!";
    }
}
