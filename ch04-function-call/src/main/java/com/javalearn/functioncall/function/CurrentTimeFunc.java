package com.javalearn.functioncall.function;

import java.util.Date;
import java.util.function.Function;


public class CurrentTimeFunc implements Function<CurrentTimeFunc.Request, CurrentTimeFunc.Response> {

    public record Request(String location) {
    }

    public record Response(String time) {
    }

    @Override
    public Response apply(Request request) {
        long l = System.currentTimeMillis();
        Date date = new Date(l-28800000);
        return new Response(date.toString());
    }

}
