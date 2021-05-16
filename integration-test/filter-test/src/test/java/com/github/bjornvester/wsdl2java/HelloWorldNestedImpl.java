package com.github.bjornvester.wsdl2java;

import com.github.bjornvester.example.nested.HelloWorldNested;
import com.github.bjornvester.example.nested.SayHi;
import com.github.bjornvester.example.nested.SayHiResponse;

public class HelloWorldNestedImpl implements HelloWorldNested {
    @Override
    public SayHiResponse sayHi(SayHi request) {
        SayHiResponse response = new SayHiResponse();
        response.setReturn("Hi, " + request.getArg0());
        return response;
    }
}
